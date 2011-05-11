/*
 * dialog preference panel
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * 発言表示の各種設定パネル。
 */
@SuppressWarnings("serial")
public class DialogPrefPanel
        extends JPanel
        implements ActionListener,
                   ItemListener {

    private final JCheckBox useBodyImage = new JCheckBox("デカキャラモード");
    private final JCheckBox useMonoImage =
            new JCheckBox("墓石を遺影に置き換える");
    private final JCheckBox isSimpleMode =
            new JCheckBox("シンプル表示モード");
    private final JCheckBox alignBaloon =
            new JCheckBox("フキダシ幅を揃える");
    private final JButton resetDefault = new JButton("出荷時に戻す");

    /**
     * コンストラクタ。
     */
    public DialogPrefPanel(){
        this.resetDefault.addActionListener(this);
        this.isSimpleMode.addItemListener(this);

        design(this);
        modifyGUIState();

        return;
    }

    /**
     * レイアウトを行う。
     * @param content コンテナ
     */
    private void design(Container content){
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        content.setLayout(layout);

        constraints.insets = new Insets(2, 2, 2, 2);

        constraints.weightx = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        content.add(this.isSimpleMode, constraints);
        content.add(this.alignBaloon, constraints);
        content.add(buildIconPanel(), constraints);

        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.SOUTHEAST;
        content.add(this.resetDefault, constraints);

        return;
    }

    /**
     * アイコン設定パネルを生成する。
     * @return アイコン設定パネル
     */
    private JComponent buildIconPanel(){
        JPanel result = new JPanel();

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        result.setLayout(layout);

        constraints.insets = new Insets(1, 1, 1, 1);

        constraints.weightx = 0.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        result.add(this.useBodyImage, constraints);
        result.add(this.useMonoImage, constraints);

        Border border = BorderFactory.createTitledBorder("アイコン表示");
        result.setBorder(border);

        return result;
    }

    /**
     * GUI間の一貫性を維持する。
     */
    private void modifyGUIState(){
        if(this.isSimpleMode.isSelected()){
            this.useBodyImage.setEnabled(false);
            this.useMonoImage.setEnabled(false);
            this.alignBaloon .setEnabled(false);
        }else{
            this.useBodyImage.setEnabled(true);
            this.useMonoImage.setEnabled(true);
            this.alignBaloon .setEnabled(true);
        }

        return;
    }

    /**
     * デカキャラモードを使うか否か画面の状態を返す。
     * @return デカキャラモードを使うならtrue
     */
    public boolean useBodyImage(){
        return this.useBodyImage.isSelected();
    }

    /**
     * 遺影モードを使うか否か画面の状態を返す。
     * @return 遺影モードを使うならtrue
     */
    public boolean useMonoImage(){
        return this.useMonoImage.isSelected();
    }

    /**
     * シンプル表示モードか否か画面の状態を返す。
     * @return シンプル表示モードならtrue
     */
    public boolean isSimpleMode(){
        return this.isSimpleMode.isSelected();
    }

    /**
     * フキダシ幅を揃えるか否か画面の状態を返す。
     * @return フキダシ幅を揃えるならtrue
     */
    public boolean alignBaloon(){
        return this.alignBaloon.isSelected();
    }

    /**
     * デカキャラモードの設定を行う。
     * @param setting 有効にするならtrue
     */
    public void setBodyImageSetting(boolean setting){
        this.useBodyImage.setSelected(setting);
        return;
    }

    /**
     * 遺影モードの設定を行う。
     * @param setting 有効にするならtrue
     */
    public void setMonoImageSetting(boolean setting){
        this.useMonoImage.setSelected(setting);
        return;
    }

    /**
     * シンプル表示モードの設定を行う。
     * @param setting 有効にするならtrue
     */
    public void setSimpleModeSetting(boolean setting){
        this.isSimpleMode.setSelected(setting);
        modifyGUIState();
        return;
    }

    /**
     * フキダシ幅揃えの設定を行う。
     * @param setting 有効にするならtrue
     */
    public void setAlignBaloonSetting(boolean setting){
        this.alignBaloon.setSelected(setting);
        return;
    }

    /**
     * 発言表示設定を設定する。
     * @param pref 表示設定
     */
    public void setDialogPref(DialogPref pref){
        setBodyImageSetting(pref.useBodyImage());
        setMonoImageSetting(pref.useMonoImage());
        setSimpleModeSetting(pref.isSimpleMode());
        setAlignBaloonSetting(pref.alignBaloonWidth());
        modifyGUIState();
        return;
    }

    /**
     * 発言表示設定を返す。
     * @return 表示設定
     */
    public DialogPref getDialogPref(){
        DialogPref result = new DialogPref();
        result.setBodyImageSetting(useBodyImage());
        result.setMonoImageSetting(useMonoImage());
        result.setSimpleMode(isSimpleMode());
        result.setAlignBalooonWidthSetting(alignBaloon());
        return result;
    }

    /**
     * デフォルトボタン押下処理。
     * @param event ボタン押下イベント
     */
    public void actionPerformed(ActionEvent event){
        Object source = event.getSource();
        if(source != this.resetDefault) return;
        this.useBodyImage.setSelected(false);
        this.useMonoImage.setSelected(false);
        this.isSimpleMode.setSelected(false);
        this.alignBaloon.setSelected(false);
        modifyGUIState();
        return;
    }

    /**
     * チェックボックス操作の受信。
     * @param event チェックボックス操作イベント
     */
    public void itemStateChanged(ItemEvent event){
        modifyGUIState();
        return;
    }

}
