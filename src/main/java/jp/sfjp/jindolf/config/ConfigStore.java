/*
 * config store
 *
 * License : The MIT License
 * Copyright(c) 2012 olyutorskii
 */

package jp.sfjp.jindolf.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Jindolf設定ディレクトリ以下に配置される各種ファイル資源の管理を行う。
 *
 * <p>管理対象は
 *
 * <ul>
 * <li>JSON設定ファイル格納ディレクトリ
 * <li>Avatar代替イメージ格納ディレクトリ
 * <li>ロックファイルの獲得/解放。
 * </ul>
 */
public class ConfigStore {

    /** ローカル画像格納ディレクトリ。 */
    public static final Path LOCALIMG_DIR = Paths.get("img");

    private static final String LOCKFILE = "lock";


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
     * @param configDirPath 設定ディレクトリ。
     */
    public ConfigStore(Path configDirPath){
        this(true, configDirPath);
        return;
    }

    /**
     * コンストラクタ。
     *
     * @param useStoreFile 設定ディレクトリ内への
     *     入出力機能を使うならtrue
     * @param configDirPath 設定ディレクトリ。
     *     設定ディレクトリを使わない場合は無視される。
     *     この時点でのディレクトリ存在の有無は関知しない。
     *     既存ディレクトリの各種属性チェックは後にチェックするものとする。
     *     nullの場合デフォルトの設定ディレクトリが用いられる。
     */
    public ConfigStore(boolean useStoreFile,
                       Path configDirPath ){
        super();

        this.useStoreFile = useStoreFile;

        if(this.useStoreFile){
            if(configDirPath != null){
                this.configDir = configDirPath;
            }else{
                this.configDir = ConfigDirUtils.getDefaultConfDirPath();
            }
        }else{
            this.configDir = null;
        }

        assert     (     this.useStoreFile  && this.configDir != null)
                || ( ( ! this.useStoreFile) && this.configDir == null);

        return;
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
     * 設定ディレクトリを返す。
     *
     * @return 設定ディレクトリ。設定ディレクトリを使わない場合はnull
     */
    public Path getConfigDir(){
        return this.configDir;
    }

    /**
     * ローカル画像格納ディレクトリを返す。
     *
     * @return 格納ディレクトリ。格納ディレクトリを使わない場合はnull
     */
    public Path getLocalImgDir(){
        if( ! this.useStoreFile ) return null;
        if(this.configDir == null) return null;

        Path result = this.configDir.resolve(LOCALIMG_DIR);

        return result;
    }

    /**
     * 設定ディレクトリの存在を確認し、なければ作る。
     *
     * <p>設定ディレクトリを使わない場合は何もしない。
     */
    public void prepareConfigDir(){
        if( ! this.useStoreFile ) return;

        if( ! Files.exists(this.configDir) ){
            Path created =
                ConfigDirUtils.buildConfDirPath(this.configDir);
            ConfigDirUtils.checkDirPerm(created);
        }else{
            ConfigDirUtils.checkDirPerm(this.configDir);
        }

        Path imgDir = this.configDir.resolve("img");
        if( ! Files.exists(imgDir) ){
            ConfigDirUtils.buildImageCacheDir(imgDir);
        }

        return;
    }

    /**
     * ロックファイルの取得を試みる。
     *
     * <p>ロックに失敗したが処理を続行する場合、
     * 設定ディレクトリは使わないものとして続行する。
     */
    public void tryLock(){
        if( ! this.useStoreFile ) return;

        File lockFile = new File(this.configDir.toFile(), LOCKFILE);
        InterVMLock lock = new InterVMLock(lockFile);

        lock.tryLock();

        if( ! lock.isFileOwner() ){
            ConfigDirUtils.confirmLockError(lock);
            if( ! lock.isFileOwner() ){
                this.useStoreFile = false;
                this.configDir = null;
            }
        }

        return;
    }

}
