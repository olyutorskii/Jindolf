/*
 * application settings
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.awt.Font;
import java.io.File;
import jp.sfjp.jindolf.data.DialogPref;
import jp.sfjp.jindolf.glyph.FontInfo;
import jp.sfjp.jindolf.net.ProxyInfo;
import jp.sourceforge.jovsonz.JsBoolean;
import jp.sourceforge.jovsonz.JsObject;
import jp.sourceforge.jovsonz.JsPair;
import jp.sourceforge.jovsonz.JsValue;

/**
 * アプリケーションの各種設定。
 */
public class AppSetting{

    private static final String HASH_PROXY = "proxy";

    private static final String HASH_FONT = "font";
    private static final String HASH_USEBODYICON = "useBodyIcon";
    private static final String HASH_USEMONOTOMB = "useMonoTomb";
    private static final String HASH_SIMPLEMODE = "isSimpleMode";
    private static final String HASH_ALIGNBALOON = "alignBaloonWidth";

    private OptionInfo optInfo;
    private ConfigStore configStore;
    private FontInfo fontInfo = FontInfo.DEFAULT_FONTINFO;

    private int frameWidth  = 800;
    private int frameHeight = 600;
    private int frameXpos = Integer.MIN_VALUE;
    private int frameYpos = Integer.MIN_VALUE;

    private ProxyInfo proxyInfo = ProxyInfo.DEFAULT;

    private DialogPref dialogPref = new DialogPref();

    private JsValue loadedNetConfig;
    private JsValue loadedTalkConfig;

    /**
     * コンストラクタ。
     */
    public AppSetting(){
        super();
        return;
    }

    /**
     * 設定格納ディレクトリ関係の解析。
     * @param optionInfo コマンドライン情報
     * @return 設定ディレクトリ情報
     */
    private static ConfigStore parseConfigStore(OptionInfo optionInfo){
        CmdOption opt =
                optionInfo.getExclusiveOption(CmdOption.OPT_CONFDIR,
                                              CmdOption.OPT_NOCONF );

        boolean useConfig;
        File configPath;

        if(opt == CmdOption.OPT_NOCONF){
            useConfig = false;
            configPath = null;
        }else if(opt == CmdOption.OPT_CONFDIR){
            String path = optionInfo.getStringArg(CmdOption.OPT_CONFDIR);
            useConfig = true;
            configPath = FileUtils.supplyFullPath(new File(path));
        }else{
            useConfig = true;
            File path = ConfigFile.getImplicitConfigDirectory();
            configPath = path;
        }

        ConfigStore result = new ConfigStore(useConfig, configPath);

        return result;
    }

    /**
     * コマンドラインオプションからアプリ設定を展開する。
     * @param optionInfo オプション情報
     */
    public void applyOptionInfo(OptionInfo optionInfo){
        this.optInfo = optionInfo;
        this.configStore = parseConfigStore(optionInfo);
        applyFontSetting();
        applyGeometrySetting();
        return;
    }

    /**
     * フォント関係の設定。
     */
    private void applyFontSetting(){
        String fontName = this.optInfo.getStringArg(CmdOption.OPT_INITFONT);

        Boolean useAntiAlias =
                this.optInfo.getBooleanArg(CmdOption.OPT_ANTIALIAS);
        if(useAntiAlias == null){
            useAntiAlias = this.fontInfo.isAntiAliased();
        }

        Boolean useFractional =
                this.optInfo.getBooleanArg(CmdOption.OPT_FRACTIONAL);
        if(useFractional == null){
            useFractional = this.fontInfo.usesFractionalMetrics();
        }

        if(fontName != null){
            Font font = Font.decode(fontName);
            this.fontInfo = this.fontInfo.deriveFont(font);
        }

        this.fontInfo =
                this.fontInfo.deriveRenderContext(useAntiAlias,
                                                  useFractional );

        return;
    }

    /**
     * ジオメトリ関係の設定。
     */
    private void applyGeometrySetting(){
        Integer ival;

        ival = this.optInfo.initialFrameWidth();
        if(ival != null) this.frameWidth = ival;

        ival = this.optInfo.initialFrameHeight();
        if(ival != null) this.frameHeight = ival;

        ival = this.optInfo.initialFrameXpos();
        if(ival != null) this.frameXpos = ival;

        ival = this.optInfo.initialFrameYpos();
        if(ival != null) this.frameYpos = ival;

        return;
    }

    /**
     * コマンドラインオプション情報を返す。
     * @return コマンドラインオプション情報
     */
    public OptionInfo getOptionInfo(){
        return this.optInfo;
    }

    /**
     * 設定格納情報を返す。
     * @return 設定格納情報
     */
    public ConfigStore getConfigStore(){
        return this.configStore;
    }

    /**
     * 初期のフレーム幅を返す。
     * @return 初期のフレーム幅
     */
    public int initialFrameWidth(){
        return this.frameWidth;
    }

    /**
     * 初期のフレーム高を返す。
     * @return 初期のフレーム高
     */
    public int initialFrameHeight(){
        return this.frameHeight;
    }

    /**
     * 初期のフレーム位置のX座標を返す。
     * 特に指示されていなければInteger.MIN_VALUEを返す。
     * @return 初期のフレーム位置のX座標
     */
    public int initialFrameXpos(){
        return this.frameXpos;
    }

    /**
     * 初期のフレーム位置のY座標を返す。
     * 特に指示されていなければInteger.MIN_VALUEを返す。
     * @return 初期のフレーム位置のY座標
     */
    public int initialFrameYpos(){
        return this.frameYpos;
    }

    /**
     * フォント設定を返す。
     * @return フォント設定
     */
    public FontInfo getFontInfo(){
        return this.fontInfo;
    }

    /**
     * フォント設定を更新する。
     * @param fontInfo フォント設定
     */
    public void setFontInfo(FontInfo fontInfo){
        this.fontInfo = fontInfo;
        return;
    }

    /**
     * プロクシ設定を返す。
     * @return プロクシ設定
     */
    public ProxyInfo getProxyInfo(){
        return this.proxyInfo;
    }

    /**
     * プロクシ設定を更新する。
     * @param proxyInfo プロクシ設定。nullならプロクシなしと解釈される。
     */
    public void setProxyInfo(ProxyInfo proxyInfo){
        if(proxyInfo == null) this.proxyInfo = ProxyInfo.DEFAULT;
        else                  this.proxyInfo = proxyInfo;
        return;
    }

    /**
     * 発言表示設定を返す。
     * @return 表示設定
     */
    public DialogPref getDialogPref(){
        return this.dialogPref;
    }

    /**
     * 発言表示設定を返す。
     * @param pref 表示設定
     */
    public void setDialogPref(DialogPref pref){
        if(pref == null) this.dialogPref = new DialogPref();
        else             this.dialogPref = pref;
        return;
    }

    /**
     * ネットワーク設定をロードする。
     */
    private void loadNetConfig(){
        JsObject root = this.configStore.loadNetConfig();
        if(root == null) return;
        this.loadedNetConfig = root;

        JsValue value = root.getValue(HASH_PROXY);
        if( ! (value instanceof JsObject) ) return;
        JsObject proxy = (JsObject) value;

        ProxyInfo info = ProxyInfo.decodeJson(proxy);

        setProxyInfo(info);

        return;
    }

    /**
     * 会話表示設定をロードする。
     */
    private void loadTalkConfig(){
        JsObject root = this.configStore.loadTalkConfig();
        if(root == null) return;
        this.loadedTalkConfig = root;

        JsValue value = root.getValue(HASH_FONT);
        if(value instanceof JsObject){
            JsObject font = (JsObject) value;
            FontInfo info = FontInfo.decodeJson(font);
            setFontInfo(info);
            applyFontSetting();
        }

        DialogPref pref = new DialogPref();
        JsBoolean boolValue;
        value = root.getValue(HASH_USEBODYICON);
        if(value instanceof JsBoolean){
            boolValue = (JsBoolean) value;
            pref.setBodyImageSetting(boolValue.booleanValue());
        }
        value = root.getValue(HASH_USEMONOTOMB);
        if(value instanceof JsBoolean){
            boolValue = (JsBoolean) value;
            pref.setMonoImageSetting(boolValue.booleanValue());
        }
        value = root.getValue(HASH_SIMPLEMODE);
        if(value instanceof JsBoolean){
            boolValue = (JsBoolean) value;
            pref.setSimpleMode(boolValue.booleanValue());
        }
        value = root.getValue(HASH_ALIGNBALOON);
        if(value instanceof JsBoolean){
            boolValue = (JsBoolean) value;
            pref.setAlignBalooonWidthSetting(boolValue.booleanValue());
        }
        setDialogPref(pref);

        return;
    }

    /**
     * ネットワーク設定をセーブする。
     */
    private void saveNetConfig(){
        if( ! getConfigStore().useStoreFile() ) return;

        JsObject root = new JsObject();
        JsObject proxy = ProxyInfo.buildJson(getProxyInfo());
        root.putValue(HASH_PROXY, proxy);

        if(this.loadedNetConfig != null){
            if(this.loadedNetConfig.equals(root)) return;
        }

        this.configStore.saveNetConfig(root);

        return;
    }

    /**
     * 会話表示設定をセーブする。
     */
    private void saveTalkConfig(){
        if( ! getConfigStore().useStoreFile() ) return;

        JsObject root = new JsObject();

        JsObject font = FontInfo.buildJson(getFontInfo());
        root.putValue(HASH_FONT, font);

        DialogPref pref = getDialogPref();
        JsPair useBodyIcon =
                new JsPair(HASH_USEBODYICON, pref.useBodyImage());
        JsPair useMonoTomb =
                new JsPair(HASH_USEMONOTOMB, pref.useMonoImage());
        JsPair isSimple =
                new JsPair(HASH_SIMPLEMODE, pref.isSimpleMode());
        JsPair alignBaloon =
                new JsPair(HASH_ALIGNBALOON, pref.alignBaloonWidth());
        root.putPair(useBodyIcon);
        root.putPair(useMonoTomb);
        root.putPair(isSimple);
        root.putPair(alignBaloon);

        if(this.loadedTalkConfig != null){
            if(this.loadedTalkConfig.equals(root)) return;
        }

        this.configStore.saveTalkConfig(root);

        return;
    }

    /**
     * 各種設定を設定格納ディレクトリからロードする。
     */
    public void loadConfig(){
        loadNetConfig();
        loadTalkConfig();
        return;
    }

    /**
     * 各種設定を設定格納ディレクトリへセーブする。
     */
    public void saveConfig(){
        saveNetConfig();
        saveTalkConfig();
        return;
    }

}
