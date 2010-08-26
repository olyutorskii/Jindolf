/*
 * dialog preferences
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

/**
 * 発言表示設定。
 */
public class DialogPref{

    private boolean useBodyImage = false;
    private boolean useMonoImage = false;
    private boolean isSimpleMode = false;
    private boolean alignBaloonWidth = false;

    /**
     * コンストラクタ。
     */
    public DialogPref(){
        super();
        return;
    }

    /**
     * デカキャラモードを使うか否か状態を返す。
     * @return デカキャラモードを使うならtrue
     */
    public boolean useBodyImage(){
        return this.useBodyImage;
    }

    /**
     * 遺影モードを使うか否か状態を返す。
     * @return 遺影モードを使うならtrue
     */
    public boolean useMonoImage(){
        return this.useMonoImage;
    }

    /**
     * シンプル表示モードを使うか否か状態を返す。
     * @return シンプルモードならtrue
     */
    public boolean isSimpleMode(){
        return this.isSimpleMode;
    }

    /**
     * バルーン幅揃えモードを使うか否か状態を返す。
     * @return バルーン幅揃えモードならtrue
     */
    public boolean alignBaloonWidth(){
        return this.alignBaloonWidth;
    }

    /**
     * デカキャラモードの設定を行う。
     * @param setting 有効にするならtrue
     */
    public void setBodyImageSetting(boolean setting){
        this.useBodyImage = setting;
        return;
    }

    /**
     * 遺影モードの設定を行う。
     * @param setting 有効にするならtrue
     */
    public void setMonoImageSetting(boolean setting){
        this.useMonoImage = setting;
        return;
    }

    /**
     * シンプルモードの設定を行う。
     * @param setting 有効にするならtrue
     */
    public void setSimpleMode(boolean setting){
        this.isSimpleMode = setting;
        return;
    }

    /**
     * バルーン幅揃えの設定を行う。
     * @param setting バルーン幅を揃えたいならtrue
     */
    public void setAlignBalooonWidthSetting(boolean setting){
        this.alignBaloonWidth = setting;
        return;
    }

    /**
     * {@inheritDoc}
     * @param obj {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj){
        if(obj instanceof DialogPref) return false;
        DialogPref target = (DialogPref) obj;

        if(this.useBodyImage     != target.useBodyImage)     return false;
        if(this.useMonoImage     != target.useMonoImage)     return false;
        if(this.isSimpleMode     != target.isSimpleMode)     return false;
        if(this.alignBaloonWidth != target.alignBaloonWidth) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode(){
        int hash;
        hash  = Boolean.valueOf(this.useBodyImage)    .hashCode() << 0;
        hash ^= Boolean.valueOf(this.useMonoImage)    .hashCode() << 4;
        hash ^= Boolean.valueOf(this.isSimpleMode)    .hashCode() << 8;
        hash ^= Boolean.valueOf(this.alignBaloonWidth).hashCode() << 12;
        return hash;
    }

}
