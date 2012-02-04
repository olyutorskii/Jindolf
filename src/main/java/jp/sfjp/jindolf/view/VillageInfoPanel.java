/*
 * village information panel
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jp.sfjp.jindolf.data.Village;

/**
 * 村情報表示パネル。
 */
@SuppressWarnings("serial")
public class VillageInfoPanel extends JPanel{

    private Village village;

    private final JLabel landName    = new JLabel();
    private final JLabel villageName = new JLabel();
    private final JLabel villageID   = new JLabel();
    private final JLabel state       = new JLabel();
    private final JLabel days        = new JLabel();
    private final JLabel limit       = new JLabel();

    private final JLabel limitCaption = new JLabel();

    /**
     * コンストラクタ。
     */
    public VillageInfoPanel(){
        super();

        design();

        updateVillage(null);

        return;
    }

    /**
     * レイアウトを行う。
     */
    private void design(){
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        setLayout(layout);

        constraints.insets = new Insets(2, 2, 2, 2);

        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        add(new JLabel("国名 : "), constraints);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(this.landName, constraints);

        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        add(new JLabel("村名 : "), constraints);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(this.villageName, constraints);

        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        add(new JLabel("村ID : "), constraints);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(this.villageID, constraints);

        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        add(new JLabel("状態 : "), constraints);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(this.state, constraints);

        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        add(new JLabel("所要日数 : "), constraints);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(this.days, constraints);

        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        add(this.limitCaption, constraints);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(this.limit, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridheight = GridBagConstraints.REMAINDER;
        add(new JPanel(), constraints);  // ダミー詰め物

        return;
    }

    /**
     * 村を返す。
     * @return 村
     */
    public Village getVillage(){
        return this.village;
    }

    /**
     * 村情報を更新する。
     * @param villageArg 村
     */
    public final void updateVillage(Village villageArg){
        this.village = villageArg;

        if(this.village == null){
            this.landName   .setText("???");
            this.villageName.setText("???");
            this.villageID  .setText("???");
            this.state      .setText("???");
            this.days       .setText("???");
            this.limit      .setText("???");

            this.limitCaption.setText("更新日時 : ");

            return;
        }

        String land  = this.village.getParentLand()
                                   .getLandDef()
                                   .getLandName();
        String vName = "「" + this.village.getVillageFullName() + "」";
        String vID   = this.village.getVillageID();

        int progressDays = this.village.getProgressDays();
        String status;
        String daysInfo;
        String caption;
        switch(this.village.getState()){
        case PROLOGUE:
            status = "プロローグ中";
            daysInfo = "プロローグ中";
            caption = "プロローグ終了予想 : ";
            break;
        case PROGRESS:
            status = "ゲーム進行中";
            daysInfo = "プロローグ + " + progressDays + "日目";
            caption = "更新日時 : ";
            break;
        case EPILOGUE:
            status = "エピローグ中";
            daysInfo = "プロローグ + " + progressDays + "日 + エピローグ中";
            caption = "エピローグ終了予想 : ";
            break;
        case GAMEOVER:
            status = "ゲーム終了";
            daysInfo = "プロローグ + " + progressDays + "日 + エピローグ";
            caption = "エピローグ終了日時 : ";
            break;
        case UNKNOWN:
            status = "不明";
            daysInfo = "不明";
            caption = "更新日時 : ";
            break;
        default:
            assert false;
            status = "???";
            daysInfo = "???";
            caption = "更新日時 : ";
            break;
        }

        int limitMonth  = this.village.getLimitMonth();
        int limitDay    = this.village.getLimitDay();
        int limitHour   = this.village.getLimitHour();
        int limitMinute = this.village.getLimitMinute();

        String limitDate = limitMonth + "月" + limitDay + "日";
        String limitTime = "";
        if(limitHour < 10) limitTime += "0";
        limitTime += limitHour + ":";
        if(limitMinute < 10) limitTime += "0";
        limitTime += limitMinute;

        this.landName   .setText(land);
        this.villageName.setText(vName);
        this.villageID  .setText(vID);
        this.state      .setText(status);
        this.days       .setText(daysInfo);
        this.limit      .setText(limitDate + " " + limitTime);

        this.limitCaption.setText(caption);

        return;
    }

}
