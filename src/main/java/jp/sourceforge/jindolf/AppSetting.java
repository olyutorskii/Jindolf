/*
 * application settings
 *
 * License : The MIT License
 * Copyright(c) 2008 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.io.File;
import jp.sourceforge.jindolf.json.JsBoolean;
import jp.sourceforge.jindolf.json.JsObject;
import jp.sourceforge.jindolf.json.JsPair;
import jp.sourceforge.jindolf.json.JsValue;

/**
 * アプリケーションの各種設定。
 */
public class AppSetting{

    private static final String NETCONFIG_FILE = "netconfig.json";
    private static final String HASH_PROXY = "proxy";

    private static final String TALKCONFIG_FILE = "talkconfig.json";
    private static final String HASH_FONT = "font";
    private static final String HASH_USEBODYICON = "useBodyIcon";
    private static final String HASH_USEMONOTOMB = "useMonoTomb";
    private static final String HASH_SIMPLEMODE = "isSimpleMode";
    private static final String HASH_ALIGNBALOON = "alignBaloonWidth";

    private OptionInfo optInfo;

    private boolean useConfigPath;
    private File configPath;

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
     * コマンドラインオプションからアプリ設定を展開する。
     * @param optionInfo オプション情報
     */
    public void applyOptionInfo(OptionInfo optionInfo){
        this.optInfo = optionInfo;
        applyConfigPathSetting();
        applyFontSetting();
        applyGeometrySetting();
        return;
    }

    /**
     * 設定格納ディレクトリ関係の設定。
     */
    private void applyConfigPathSetting(){
        CmdOption opt = this.optInfo
                .getExclusiveOption(CmdOption.OPT_CONFDIR,
                                    CmdOption.OPT_NOCONF );
        if(opt == CmdOption.OPT_NOCONF){
            this.useConfigPath = false;
            this.configPath = null;
        }else if(opt == CmdOption.OPT_CONFDIR){
            this.useConfigPath = true;
            String path = this.optInfo.getStringArg(CmdOption.OPT_CONFDIR);
            this.configPath = FileUtils.supplyFullPath(new File(path));
        }else{
            this.useConfigPath = true;
            File path = ConfigFile.getImplicitConfigDirectory();
            this.configPath = path;
        }

        return;
    }

    /**
     * フォント関係の設定。
     */
    private void applyFontSetting(){
        String fontName = this.optInfo.getStringArg(CmdOption.OPT_INITFONT);
        Boolean useAntiAlias =
                this.optInfo.getBooleanArg(CmdOption.OPT_ANTIALIAS);
        Boolean useFractional =
                this.optInfo.getBooleanArg(CmdOption.OPT_FRACTIONAL);

        if(fontName != null){
            Font font = Font.decode(fontName);
            this.fontInfo = this.fontInfo.deriveFont(font);
        }

        if(useAntiAlias != null){
            FontRenderContext context = this.fontInfo.getFontRenderContext();
            FontRenderContext newContext =
                    new FontRenderContext(context.getTransform(),
                                          useAntiAlias.booleanValue(),
                                          context.usesFractionalMetrics() );
            this.fontInfo = this.fontInfo.deriveRenderContext(newContext);
        }

        if(useFractional != null){
            FontRenderContext context = this.fontInfo.getFontRenderContext();
            FontRenderContext newContext =
                    new FontRenderContext(context.getTransform(),
                                          context.isAntiAliased(),
                                          useFractional.booleanValue() );
            this.fontInfo = this.fontInfo.deriveRenderContext(newContext);
        }

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
     * 設定格納ディレクトリを返す。
     * @return 設定格納ディレクトリ。
     */
    public File getConfigPath(){
        return this.configPath;
    }

    /**
     * 設定格納ディレクトリを設定する。
     * @param path 設定格納ディレクトリ
     */
    public void setConfigPath(File path){
        this.configPath = path;
        return;
    }

    /**
     * 設定格納ディレクトリを使うか否かを返す。
     * @return 使うならtrue
     */
    public boolean useConfigPath(){
        return this.useConfigPath;
    }

    /**
     * 設定格納ディレクトリを使うか否か設定する。
     * @param need 使うならtrue
     */
    public void setUseConfigPath(boolean need){
        this.useConfigPath = need;
        return;
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
        if( ! useConfigPath() ) return;

        JsValue value = ConfigFile.loadJson(new File(NETCONFIG_FILE));
        if(value == null) return;
        this.loadedNetConfig = value;

        if( ! (value instanceof JsObject) ) return;
        JsObject root = (JsObject) value;

        value = root.getValue(HASH_PROXY);
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
        if( ! useConfigPath() ) return;

        JsValue value = ConfigFile.loadJson(new File(TALKCONFIG_FILE));
        if(value == null) return;
        this.loadedTalkConfig = value;

        if( ! (value instanceof JsObject) ) return;
        JsObject root = (JsObject) value;

        value = root.getValue(HASH_FONT);
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
        if( ! useConfigPath() ) return;

        JsObject root = new JsObject();
        JsObject proxy = ProxyInfo.buildJson(getProxyInfo());
        root.putValue(HASH_PROXY, proxy);

        if(this.loadedNetConfig != null){
            if(this.loadedNetConfig.equals(root)) return;
        }

        ConfigFile.saveJson(new File(NETCONFIG_FILE), root);

        return;
    }

    /**
     * 会話表示設定をセーブする。
     */
    private void saveTalkConfig(){
        if( ! useConfigPath() ) return;

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

        ConfigFile.saveJson(new File(TALKCONFIG_FILE), root);

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
