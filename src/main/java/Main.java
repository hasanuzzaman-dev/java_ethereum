import contracts.MyContract;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class Main {


 //   private final static String PRIVATE_KEY = "b05f065d0a76691c024a5291b83a86caf2a473f11ef31faa4ff02e92a2c0cb23";
    private final static String PRIVATE_KEY = "9d1f9842cc77a9050460d5c6af0b6adabde5a535f6205cc2236826a1b35d8205";
    private final static BigInteger GAS_LIMIT = BigInteger.valueOf(6721975L);
    private final static BigInteger GAS_PRICE = BigInteger.valueOf(20000000000L);

    private final static String RECIPIENT = "0xcBE052Af2d5c7D28b7f6dfBA573DCE95ea979921";
    // private final static String CONTRACT_ADDRESS = "0x0584e0b9bb894f3d335ee0022854b5d1faf62bf2";
    public static void main(String[] args) throws Exception {

        new Main();


        /*
        System.out.println("Connecting to Ethereum ...");
        Web3j web3 = Web3j.build(new HttpService("https://eth-rinkeby.alchemyapi.io/v2/ukXBvGXFSkA7R3alQlK8Qg8qCBvLql3s"));
        System.out.println("Successfully connected to Ethereum");

       try {
            // web3_clientVersion returns the current client version.
            Web3ClientVersion clientVersion = web3.web3ClientVersion().send();

            // eth_blockNumber returns the number of most recent block.
            EthBlockNumber blockNumber = web3.ethBlockNumber().send();

            // eth_gasPrice, returns the current price per gas in wei.
            EthGasPrice gasPrice = web3.ethGasPrice().send();

            //Get balance result synchronously
            EthGetBalance balanceResult = web3.ethGetBalance("0x05D71C9465Aae514ee284a5CA0De8eB2Dd8037B7",
                    DefaultBlockParameterName.LATEST).send();

            //Obtain the BigInteger balance representation, in the wei unit.
            BigInteger balanceInWei = balanceResult.getBalance();

            //Obtain the BigDecimal balance representation, in the ETH unit.

            BigDecimal balanceInEther = Convert.fromWei(balanceInWei.toString(), Convert.Unit.ETHER);

           // Print result
            System.out.println("Client version: " + clientVersion.getWeb3ClientVersion());
            System.out.println("Block number: " + blockNumber.getBlockNumber());
            System.out.println("Gas price: " + gasPrice.getGasPrice());
            System.out.println("balanceInWei: " + balanceInWei);
            System.out.println("balanceInEth: " + balanceInEther);

        } catch (IOException ex) {
            throw new RuntimeException("Error whilst sending json-rpc requests", ex);
        }*/
    }

    private Main() throws Exception {
       // Web3j web3j = Web3j.build(new HttpService("http://localhost:7545"));
        Web3j web3j = Web3j.build(new HttpService("https://eth-rinkeby.alchemyapi.io/v2/ukXBvGXFSkA7R3alQlK8Qg8qCBvLql3s"));
        System.out.println("Successfully connected to Ethereum");

        TransactionManager transactionManager = new RawTransactionManager(
                web3j,
                getCredentialsFromPrivateKey()
        );

        ContractGasProvider contractGasProvider = new ContractGasProvider() {
            @Override
            public BigInteger getGasPrice(String s) {
                return GAS_PRICE;
            }

            @Override
            public BigInteger getGasPrice() {
                return null;
            }

            @Override
            public BigInteger getGasLimit(String s) {
                return GAS_LIMIT;
            }

            @Override
            public BigInteger getGasLimit() {
                return null;
            }
        };

        String deployAddress = deployContract(web3j,transactionManager,contractGasProvider);
        System.out.println("DeployAddress: "+deployAddress);

        MyContract myContract = loadContract(deployAddress,web3j,transactionManager,contractGasProvider);

        myContract
                .setMyUnit(BigInteger.valueOf(12L))
                .send();

        BigInteger num = myContract.myUint().send();
        System.out.println("Number: "+num);




    }

    private String deployContract(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) throws Exception {
        return MyContract
                .deploy(web3j,transactionManager,contractGasProvider)
                .send()
                .getContractAddress();
    }

    private MyContract loadContract(String contractAddress, Web3j web3j,TransactionManager transactionManager, ContractGasProvider contractGasProvider){
        return MyContract.load(contractAddress,web3j,transactionManager,contractGasProvider);
    }

    private void transferEthereum(Web3j web3j, TransactionManager transactionManager) throws Exception {
        Transfer transfer = new Transfer(web3j, transactionManager);

        TransactionReceipt transactionReceipt = transfer.sendFunds(
                RECIPIENT,
                BigDecimal.ONE,
                Convert.Unit.ETHER,
                GAS_PRICE,
                GAS_LIMIT
        ).send();

        System.out.print("Transaction = " + transactionReceipt.getTransactionHash());
    }

    private void printWeb3Version(Web3j web3j) {
        Web3ClientVersion web3ClientVersion = null;
        try {
            web3ClientVersion = web3j.web3ClientVersion().send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String web3ClientVersionString = web3ClientVersion.getWeb3ClientVersion();
        System.out.println("Web3 client version: " + web3ClientVersionString);
    }

    private Credentials getCredentialFromWallet() throws CipherException, IOException {
        return WalletUtils.loadCredentials(
                "passphrase",
                "wallet/path"
        );
    }

    private Credentials getCredentialsFromPrivateKey() {
        return Credentials.create(PRIVATE_KEY);
    }
}
