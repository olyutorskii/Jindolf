/*
 * config store
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Jindolf設定ディレクトリの管理を行う。
 *
 * <p>デフォルトの設定ディレクトリや
 * アプリのコマンドライン引数から構成された、
 * 設定ディレクトリに関する情報が保持管理される。
 *
 * <p>基本的に1アプリのみが設定ディレクトリへの入出力を許される。
 *
 * <p>インスタンス生成後に
 * 設定ディレクトリを使わない設定に変更することが可能。
 * (※ロック確保失敗後の続行等を想定)
 *
 * <p>設定ディレクトリには
 *
 * <ul>
 * <li>ロックファイル
 * <li>JSON設定ファイル
 * <li>Avatar代替イメージ格納ディレクトリ
 * </ul>
 *
 * <p>などが配置される。
 *
 * <p>コンストラクタに与えられるディレクトリは
 * 絶対パスでなければならない。
 *
 * <p>ロックファイル取得の失敗、
 * およびその後のユーザインタラクションによっては、
 * 設定ディレクトリを使わない設定に上書きされた後
 * 起動処理が続行される場合がありうる。
 */
public class ConfigStore {

    private static final Path JINCONF      = Paths.get("Jindolf");
    private static final Path JINCONF_DOT  = Paths.get(".jindolf");
    private static final Path LOCKFILE     = Paths.get("lock");
    private static final Path LOCALIMG_DIR = Paths.get("img");

    private static final Path MAC_LIB     = Paths.get("Library");
    private static final Path MAC_APPSUPP = Paths.get("Application Support");


    private boolean useStoreFile;
    private Path configDir;


    /**
     * コンストラクタ。
     *
     * <p>このインスタンスでは、
     * 設定ディレクトリへの入出力を行わない。
     */
    public ConfigStore(){
        this(false, null);
        return;
    }

    /**
     * コンストラクタ。
     *
     * <p>このインスタンスでは、
     * 設定ディレクトリへの入出力を行うが、
     * デフォルトではない明示されたディレクトリが用いられる。
     *
     * @param configDirPath 設定ディレクトリの絶対パス。
     *     nullの場合はデフォルトの設定ディレクトリが用いられる。
     * @throws IllegalArgumentException 絶対パスではない。
     */
    public ConfigStore(Path configDirPath) throws IllegalArgumentException{
        this(true, configDirPath);
        return;
    }

    /**
     * コンストラクタ。
     *
     * @param useStoreFile 設定ディレクトリ内への
     *     入出力機能を使うならtrue
     * @param configDirPath 設定ディレクトリの絶対パス。
     *     設定ディレクトリを使わない場合は無視される。
     *     この時点でのディレクトリ存在の有無は関知しない。
     *     既存ディレクトリの各種属性チェックは後にチェックするものとする。
     *     nullの場合デフォルトの設定ディレクトリが用いられる。
     * @throws IllegalArgumentException 絶対パスではない。
     */
    protected ConfigStore(boolean useStoreFile,
                          Path configDirPath )
            throws IllegalArgumentException{
        super();

        this.useStoreFile = useStoreFile;

        if(this.useStoreFile){
            if(configDirPath != null){
                if( ! configDirPath.isAbsolute()){
                    throw new IllegalArgumentException();
                }
                this.configDir = configDirPath;
            }else{
                this.configDir = getDefaultConfDirPath();
            }
        }else{
            this.configDir = null;
        }

        assert     (     this.useStoreFile  && this.configDir != null)
                || ( ( ! this.useStoreFile) && this.configDir == null);

        return;
    }


    /**
     * 暗黙的な設定格納ディレクトリを絶対パスで返す。
     *
     * <ul>
     *
     * <li>起動元JARファイルと同じディレクトリに、
     * アクセス可能なディレクトリ"Jindolf"が
     * すでに存在していればそれを返す。
     *
     * <li>起動元JARファイルおよび"Jindolf"が発見できなければ、
     * MacOSX環境の場合"~/Library/Application Support/Jindolf/"を返す。
     * Windows環境の場合"%USERPROFILE%\Jindolf\"を返す。
     *
     * <li>それ以外の環境(Linux,etc?)の場合"~/.jindolf/"を返す。
     *
     * </ul>
     *
     * <p>返すディレクトリが存在しているか否か、
     * アクセス可能か否かは呼び出し元で判断せよ。
     *
     * @return 設定格納ディレクトリの絶対パス
     */
    public static Path getDefaultConfDirPath(){
        Path jarParent = FileUtils.getJarDirectory();
        if(FileUtils.isAccessibleDirectory(jarParent)){
            Path confPath = jarParent.resolve(JINCONF);
            if(FileUtils.isAccessibleDirectory(confPath)){
                assert confPath.isAbsolute();
                return confPath;
            }
        }

        Path appset = getAppSetDir();

        Path leaf;
        if(FileUtils.isMacOSXFs() || FileUtils.isWindowsOSFs()){
            leaf = JINCONF;
        }else{
            leaf = JINCONF_DOT;
        }

        Path result = appset.resolve(leaf);
        assert result.isAbsolute();

        return result;
    }

    /**
     * アプリケーション設定ディレクトリを絶対パスで返す。
     *
     * <p>存在の有無、アクセスの可否は関知しない。
     *
     * <p>WindowsやLinuxではホームディレクトリ。
     * Mac OS X ではさらにホームディレクトリの下の
     * "Library/Application Support/"
     *
     * @return アプリケーション設定ディレクトリの絶対パス
     */
    private static Path getAppSetDir(){
        Path home = getHomeDirectory();

        Path result = home;

        if(FileUtils.isMacOSXFs()){
            result = result.resolve(MAC_LIB);
            result = result.resolve(MAC_APPSUPP);
        }

        return result;
    }

    /**
     * ホームディレクトリを得る。
     *
     * <p>システムプロパティuser.homeで示されたホームディレクトリを
     * 絶対パスで返す。
     *
     * @return ホームディレクトリの絶対パス。
     */
    private static Path getHomeDirectory(){
        String homeProp = System.getProperty("user.home");
        Path result = Paths.get(homeProp);
        result = result.toAbsolutePath();
        return result;
    }


    /**
     * 設定ディレクトリを使うか否か判定する。
     *
     * @return 設定ディレクトリを使うならtrue。
     */
    public boolean useStoreFile(){
        return this.useStoreFile;
    }

    /**
     * 設定ディレクトリを絶対パスで返す。
     *
     * @return 設定ディレクトリの絶対パス。
     *     設定ディレクトリを使わない場合はnull
     */
    public Path getConfigDir(){
        return this.configDir;
    }

    /**
     * 設定ディレクトリを使わない設定に変更する。
     */
    public void setNoConf(){
        this.useStoreFile = false;
        this.configDir = null;
        return;
    }

    /**
     * ローカル画像格納ディレクトリを絶対パスで返す。
     *
     * @return 格納ディレクトリの絶対パス。
     *     格納ディレクトリを使わない場合はnull
     */
    public Path getLocalImgDir(){
        if( ! this.useStoreFile ) return null;
        if(this.configDir == null) return null;

        Path result = this.configDir.resolve(LOCALIMG_DIR);
        assert result.isAbsolute();

        return result;
    }

    /**
     * ロックファイルを絶対パスで返す。
     *
     * @return ロックファイルの絶対パス。
     *     格納ディレクトリを使わない場合はnull
     */
    public Path getLockFile(){
        if( ! this.useStoreFile ) return null;
        if(this.configDir == null) return null;

        Path lockFile = this.configDir.resolve(LOCKFILE);

        return lockFile;
    }

}
