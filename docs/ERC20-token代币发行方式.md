## ERC20-token代币发行方式

### 环境搭建

1. 下载chrome浏览器，安装metamask插件，需要科学上网，插件地址：(https://chrome.google.com/webstore/detail/metamask/nkbihfbeogaeaoehlefnkodbefgpgknn?hl=cn)[https://chrome.google.com/webstore/detail/metamask/nkbihfbeogaeaoehlefnkodbefgpgknn?hl=cn]

2. 通过metamask创建ETH钱包，生成钱包地址，并向地址中转入适量ETH作为发布token的手续费。


### 获取token合约代码
1. 进入网址：(https://etherscan.io/tokens)[https://etherscan.io/tokens]，有所有的token代币可供选择，如：USDT,BNB

2. 任意选择一个点击进入，如： HuobiToken (HT)，再将url链接中的token修改为address，再点击进入(https://etherscan.io/address/0x6f259637dcd74c767781e37bc6133cd6a68aa161)[https://etherscan.io/address/0x6f259637dcd74c767781e37bc6133cd6a68aa161]

3. 再选择页面中的合约选项，便能查看到Solidity的合约代码

4. 查看合约的最终URL为：(https://etherscan.io/address/0x6f259637dcd74c767781e37bc6133cd6a68aa161#code)[https://etherscan.io/address/0x6f259637dcd74c767781e37bc6133cd6a68aa161#code]

5. 将代码拷贝，再打开网址：(https://remix.ethereum.org)[https://remix.ethereum.org]，在网址中创建一个文件，并将代码粘贴



### 修改合约代码
获取到代码合约HBToken这一段，对币名，发行数等记录进行修改，代码注释如下：

```js
//类名修改 HBToken -> NiceToken
//每种token代币的合约代码都不相同，功能也不同，有些代币是在构造函数中输入参数对其进行初始化的，比如：USDT
contract NiceToken is UnboundedRegularToken {

    //总发行数量 5乘以10的16次方，如果有精度，则需要减去精度的次方数。如精度为8，则总发行数量为 5*10**8
    uint public totalSupply = 5*10**16;
    //精度
    uint8 constant public decimals = 8;
    //代币名称
    string constant public name = "NiceToken";
    //代币符号
    string constant public symbol = "NICE";

    function NiceToken() {
        balances[msg.sender] = totalSupply;
        Transfer(address(0), msg.sender, totalSupply);
    }
}
```

### 发布代币

1. 修改好代码后点击右边栏的第三个`SOLIDITY COMPILER`进行代码编译，需要在`Compiler`选项中选中代码第一行`pragma solidity 0.4.19;`中的版本

2. 然后再点击右边栏第四个`DEPLOY & RUN TRANSACTIONS,Environment`选择`Injected Web3`，弹出`metamask`后确认就OK

3. 在拉下选项中选中`ZXLToken-browser/zxl_coin.sol`,再点击`Deploy`按钮部署，确认下去就完全OK了!


### 验证合约
1. 通过合约地址进入合约界面：`https://etherscan.io/address/0x198ef315081236928121c1197fe4aa098905f2c1`

2. 点击合约选项，再点击立即验证并发布(`Verify and Publish`)

3. 选择代码中相对应的`solidity`版本再继续，将代码拷贝进入代码框中，再进行人机身份验证之后点击`Verify and Publish`,就验证完成辣！