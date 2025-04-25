package com.payment.sol.task;

import com.payment.core.entity.Currency;
import com.payment.core.entity.Height;
import com.payment.core.entity.Recharge;
import com.payment.core.enums.UpchainStatusEnum;
import com.payment.core.service.CurrencyService;
import com.payment.core.service.RechargeService;
import com.payment.core.utils.AssertUtils;
import com.payment.sol.service.SolService;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Utils;
import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.rpc.types.Block;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class SolScanTask {


    @Value("${sol.currencyName}")
    private String currencyName;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private RechargeService rechargeService;

    @Resource
    private SolService solService;

    /**
     * 扫描链上的交易是否和数据库中的充值单是否匹配，如果匹配则修改对应状态。
     * 在最近的300个区块的出块时间一般平均为15秒。
     * 定时任务使用10秒间隔（10 * 1000）。
     * https://txstreet.com/
     */
    @Scheduled(fixedDelay = 10 * 1000)
    public void scanOrder() {
        //获取当前货币的配置信息
        Currency ethInfo = currencyService.findCurrency(currencyName);
        AssertUtils.isNotNull(ethInfo, "数据库未配置货币信息：" + currencyName);

        //获取到当前与网络区块高度
        Long networkBlockHeight = solService.getBlockHeight();
        Height heightObj = rechargeService.getCurrentHeight(currencyName);
        if(heightObj == null) {
            Height height = new Height();
            height.setCurrencyId(ethInfo.getId());
            height.setCurrencyName(ethInfo.getCurrencyName());
            height.setCurrentHeight(networkBlockHeight.intValue());
            height.setUpdatedAt(new Date());
            rechargeService.saveCurrentHeight(height);
            return;
        }

        Integer currentHeight = heightObj.getCurrentHeight();

        //相隔1个区块不进行扫描
        AssertUtils.isFalse(networkBlockHeight - currentHeight <= 1, "不存在需要扫描的区块");

        //扫描区块中的交易
        for(Integer i = currentHeight + 1; i <= networkBlockHeight; i++) {
            Block block = solService.getBlockByNumber(i.longValue());

            List<ConfirmedTransaction> transactions = block.getTransactions();

            for (ConfirmedTransaction confirmedTransaction : transactions) {
                ConfirmedTransaction.Transaction transaction = confirmedTransaction.getTransaction();

                ConfirmedTransaction.Message message = transaction.getMessage();
                // 获取交易指令
                List<ConfirmedTransaction.Instruction> instructions = message.getInstructions();

                List<String> accountKeys = message.getAccountKeys();

                // 是否是transfer交易
                for (ConfirmedTransaction.Instruction instruction : instructions) {
                    String programId = accountKeys.get(Math.toIntExact(instruction.getProgramIdIndex()));
                    if(Objects.equals(programId, SystemProgram.PROGRAM_ID.toBase58())&& instruction.getData().getBytes().length==8){

                    }
                }

                for (ConfirmedTransaction.Instruction instruction :instructions){
                    String fromAddress =  accountKeys.get(Math.toIntExact(instruction.getAccounts().get(0)));
                    String toAddress = accountKeys.get(Math.toIntExact(instruction.getAccounts().get(1)));

                    if(StringUtils.isEmpty(toAddress)) {
                        log.info("交易{}不存在toAddress", toAddress);
                        continue;
                    }

                    byte[] data = instruction.getData().getBytes();
                    int lamports = Utils.readUint16(data, 4);


                    BigDecimal amount = BigDecimal.valueOf(lamports);
                    Recharge recharge = rechargeService.getRecharge(toAddress, currencyName, amount);
                    if(recharge == null) {
                        log.info("地址不在库中：{}", toAddress);
                        continue;
                    }
                    recharge.setFromAddress(fromAddress);
                    recharge.setTxHash(transaction.getSignatures().get(0));
                    recharge.setCurrentConfirm(BigInteger.valueOf(Long.parseLong(block.getBlockHeight())).subtract(BigInteger.valueOf(i)).intValue());
                    recharge.setHeight( Integer.parseInt(block.getBlockHeight()));
                    recharge.setUpchainAt(new Date(block.getBlockTime()));
                    recharge.setUpdatedAt(new Date());

                    if(i - Integer.parseInt(block.getBlockHeight())>= ethInfo.getConfirms()) {
                        recharge.setUpchainStatus(UpchainStatusEnum.SUCCESS.getCode());
                        recharge.setUpchainSuccessAt(new Date(block.getBlockTime()));
                    }else {
                        recharge.setUpchainStatus(UpchainStatusEnum.WAITING_CONFIRM.getCode());
                    }
                    rechargeService.updateRecharge(recharge);
                }
            }

        }

        //更新区块高度
        heightObj.setCurrentHeight(networkBlockHeight.intValue());
        heightObj.setUpdatedAt(new Date());
        rechargeService.saveCurrentHeight(heightObj);
    }



    /**
     * 确认交易，将数据库中状态为待确认的充值单再次去链上查询是否确认数超过了配置确认数。
     * 在最近的300个区块的出块时间一般平均为15秒。
     * 定时任务使用15秒间隔（15 * 1000）。
     * https://txstreet.com/
     */
    @Scheduled(fixedDelay = 10 * 1000)
    public void confirmTx() {
        //0. 获取当前货币的配置信息
        Currency ethInfo = currencyService.findCurrency(currencyName);
        AssertUtils.isNotNull(ethInfo, "数据库未配置货币信息：" + currencyName);

        //1. 获取当前网络的区块高度
        Long currentHeight = solService.getBlockHeight();

        //2. 查询到所有待确认的充值单
        List<Recharge> waitConfirmRecharge = rechargeService.getWaitConfirmRecharge(currencyName);
        AssertUtils.isNotNull(waitConfirmRecharge, "不存在待确认的充值单");

        //3. 遍历库中交易进行判断是否成功
        for (Recharge recharge : waitConfirmRecharge) {
            ConfirmedTransaction transaction = solService.getTransactionByHash(recharge.getTxHash());

            //如果链上交易确认数大于等于配置的确认数，则更新充值单为成功并更新上链成功时间，否则只更新当前确认数。
            if(currentHeight - recharge.getHeight()  >= ethInfo.getConfirms()) {
                recharge.setUpchainStatus(UpchainStatusEnum.SUCCESS.getCode());
                recharge.setUpchainSuccessAt(new Date());
            }
            recharge.setCurrentConfirm((int) (currentHeight - recharge.getHeight()));
            recharge.setUpdatedAt(new Date());

            rechargeService.saveRecharge(recharge);
        }
    }

}
