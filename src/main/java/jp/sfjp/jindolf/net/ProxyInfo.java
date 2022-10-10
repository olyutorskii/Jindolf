/*
 * proxy information
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.net;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import jp.sourceforge.jovsonz.JsNumber;
import jp.sourceforge.jovsonz.JsObject;
import jp.sourceforge.jovsonz.JsPair;
import jp.sourceforge.jovsonz.JsString;
import jp.sourceforge.jovsonz.JsValue;

/**
 * プロクシ情報。
 * SOCKSも範疇に入る。
 * IP4,IP6以外のアドレスは未サポート。
 */
public class ProxyInfo{

    /**
     * アドレス「0.0.0.0」、ポート番号0のソケット端点。
     */
    public static final InetSocketAddress IP4SOCKET_NOBODY =
            InetSocketAddress.createUnresolved("0.0.0.0", 0);

    /** デフォルトのプロクシ(直接接続)。 */
    public static final ProxyInfo DEFAULT = new ProxyInfo();

    private static final String HASH_TYPE = "type";
    private static final String HASH_HOST = "host";
    private static final String HASH_PORT = "port";


    private final Proxy proxy;
    private final InetSocketAddress inetAddr;


    /**
     * コンストラクタ。
     * 直接接続プロクシが暗黙に指定される。
     */
    public ProxyInfo(){
        this(Proxy.NO_PROXY);
        return;
    }

    /**
     * コンストラクタ。
     * @param proxy プロクシ
     */
    public ProxyInfo(Proxy proxy){

        this.proxy = proxy;

        if(this.proxy.type() == Proxy.Type.DIRECT){
            this.inetAddr = IP4SOCKET_NOBODY;
        }else{
            SocketAddress addr = this.proxy.address();
            if( ! (addr instanceof InetSocketAddress) ){
                throw new IllegalArgumentException();
            }
            this.inetAddr = (InetSocketAddress) addr;
        }

        return;
    }

    /**
     * コンストラクタ。
     * @param type プロクシの種別
     * @param hostName ホスト名
     * @param port ポート番号
     */
    public ProxyInfo(Proxy.Type type, String hostName, int port){
        this(type, InetSocketAddress.createUnresolved(hostName, port));
        return;
    }

    /**
     * コンストラクタ。
     * @param type プロクシの種別
     * @param inetAddr 端点
     */
    public ProxyInfo(Proxy.Type type, InetSocketAddress inetAddr){
        super();

        if(type == null || inetAddr == null){
            throw new NullPointerException();
        }

        if(type == Proxy.Type.DIRECT){
            this.proxy = Proxy.NO_PROXY;
        }else{
            this.proxy = new Proxy(type, inetAddr);
        }

        this.inetAddr = inetAddr;

        return;
    }


    /**
     * プロクシ設定をJSON形式にエンコードする。
     * @param proxyInfo プロクシ設定
     * @return JSON object
     */
    public static JsObject buildJson(ProxyInfo proxyInfo){
        JsPair type = new JsPair(HASH_TYPE, proxyInfo.getType().name());
        JsPair host = new JsPair(HASH_HOST, proxyInfo.getHostName());
        JsPair port = new JsPair(HASH_PORT, proxyInfo.getPort());

        JsObject result = new JsObject();
        result.putPair(type);
        result.putPair(host);
        result.putPair(port);

        return result;
    }

    /**
     * JSONからのプロクシ設定復元。
     * @param obj JSON object
     * @return 復元されたプロクシ設定。
     */
    public static ProxyInfo decodeJson(JsObject obj){
        JsValue value;

        Proxy.Type type = Proxy.Type.DIRECT;
        value = obj.getValue(HASH_TYPE);
        if(value instanceof JsString){
            JsString string = (JsString) value;
            try{
                type = Enum.valueOf(Proxy.Type.class, string.toRawString());
            }catch(IllegalArgumentException e){
                // NOTHING
            }
        }

        String host = "0.0.0.0";
        value = obj.getValue(HASH_HOST);
        if(value instanceof JsString){
            JsString string = (JsString) value;
            host = string.toRawString();
        }

        int port = 0;
        value = obj.getValue(HASH_PORT);
        if(value instanceof JsNumber){
            JsNumber number = (JsNumber) value;
            port = number.intValue();
        }

        return new ProxyInfo(type, host, port);
    }

    /**
     * {@inheritDoc}
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj){
        if(obj == null) return false;
        if(getClass() != obj.getClass()) return false;
        ProxyInfo target = (ProxyInfo) obj;

        boolean result;

        result = this.proxy.equals(target.proxy);
        if(result){
            result = this.inetAddr.equals(target.inetAddr);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        return this.proxy.hashCode() ^ this.inetAddr.hashCode();
    }

    /**
     * プロクシを返す。
     * 直結プロクシだった場合、ホスト名とポート番号には何が入っているか不明。
     * @return プロクシ
     */
    public Proxy getProxy(){
        return this.proxy;
    }

    /**
     * プロクシ種別を返す。
     * @return プロクシ種別
     */
    public Proxy.Type getType(){
        return this.proxy.type();
    }

    /**
     * ソケット端点を返す。
     * @return ソケット端点
     */
    public InetSocketAddress address(){
        return this.inetAddr;
    }

    /**
     * ホスト名を返す。
     * @return ホスト名
     */
    public String getHostName(){
        return this.inetAddr.getHostName();
    }

    /**
     * ポート番号を返す。
     * @return ポート番号
     */
    public int getPort(){
        return this.inetAddr.getPort();
    }

    // TODO 認証情報のサポート
}
