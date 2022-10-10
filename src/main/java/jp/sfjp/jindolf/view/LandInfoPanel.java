/*
 * Land information panel
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jp.sfjp.jindolf.data.Land;
import jp.sfjp.jindolf.dxchg.WebButton;
import jp.sfjp.jindolf.util.Monodizer;
import jp.sourceforge.jindolf.corelib.LandDef;
import jp.sourceforge.jindolf.corelib.LandState;

/**
 * 国情報表示パネル。
 */
@SuppressWarnings("serial")
public class LandInfoPanel extends JPanel{

    private final JLabel landName       = new JLabel();
    private final JLabel landIdentifier = new JLabel();
    private final WebButton webURL      = new WebButton();
    private final JLabel startDate      = new JLabel();
    private final JLabel endDate        = new JLabel();
    private final JLabel landState      = new JLabel();
    private final JLabel locale         = new JLabel();
    private final JLabel timezone       = new JLabel();
    private final WebButton contact     = new WebButton();
    private final JLabel description    = new JLabel();


    /**
     * コンストラクタ。
     */
    public LandInfoPanel(){
        super();

        Monodizer.monodize(this.landIdentifier);
        Monodizer.monodize(this.locale);

        design();

        return;
    }


    /**
     * 国の状態を文字列化する。
     * @param state 国状態
     * @return 文字列化された国状態
     */
    private static String getStatusMark(LandState state){
        String result;

        switch(state){
        case CLOSED:     result = "サービス終了";     break;
        case HISTORICAL: result = "過去ログ提供のみ"; break;
        case ACTIVE:     result = "稼動中";           break;
        default:
            assert false;
            result = "";
            break;
        }

        return result;
    }

    /**
     * 一行分レイアウトする。
     * @param item 項目名
     * @param comp コンポーネント
     */
    private void layoutRow(String item, JComponent comp){
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(2, 2, 2, 2);

        String itemCaption = item + " : ";
        JLabel itemLabel = new JLabel(itemCaption);

        constraints.anchor = GridBagConstraints.EAST;
        constraints.gridwidth = 1;
        add(itemLabel, constraints);

        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        add(comp, constraints);

    }

    /**
     * レイアウトを行う。
     */
    private void design(){
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        layoutRow("国名",      this.landName);
        layoutRow("識別名",    this.landIdentifier);
        layoutRow("Webサイト", this.webURL);
        layoutRow("建国",      this.startDate);
        layoutRow("亡国",      this.endDate);
        layoutRow("状態",      this.landState);
        layoutRow("ロケール",  this.locale);
        layoutRow("時間帯",    this.timezone);
        layoutRow("連絡先",    this.contact);
        layoutRow("説明",      this.description);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridheight = GridBagConstraints.REMAINDER;
        add(new JPanel(), constraints);  // ダミー詰め物

        return;
    }

    /**
     * 国情報を更新する。
     * @param land 国
     */
    public void update(Land land){
        LandDef landDef = land.getLandDef();

        DateFormat dform =
            DateFormat.getDateTimeInstance(DateFormat.FULL,
                                           DateFormat.FULL);

        long start = landDef.getStartDateTime();
        String startStr = dform.format(new Date(start));
        if(start < 0){
            startStr = "(不明)";
        }

        long end   = landDef.getEndDateTime();
        String endStr = dform.format(new Date(end));
        if(end < 0){
            endStr = "まだまだ";
        }

        String status = getStatusMark(land.getLandDef().getLandState());

        this.landName       .setText(landDef.getLandName());
        this.landIdentifier .setText(landDef.getLandId());
        this.webURL         .setURI(land.getLandDef().getWebURI());
        this.startDate      .setText(startStr);
        this.endDate        .setText(endStr);
        this.landState      .setText(status);
        this.locale         .setText(landDef.getLocale().toString());
        this.timezone       .setText(landDef.getTimeZone().getDisplayName());
        this.contact        .setURLText(landDef.getContactInfo());
        this.description    .setText(landDef.getDescription());

        revalidate();

        return;
    }

}
