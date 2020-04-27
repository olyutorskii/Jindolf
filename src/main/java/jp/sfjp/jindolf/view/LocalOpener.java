/*
 * Local file opener panel
 *
 * License : The MIT License
 * Copyright(c) 2020 olyutorskii
 */

package jp.sfjp.jindolf.view;

import java.awt.LayoutManager;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * ローカルなアーカイブXMlファイルの読み込みを指示するためのGUI。
 */
final class LocalOpener extends JPanel{

    private JButton openXml = new JButton("アーカイブXMLを開く...");

    private JPanel dandd = new JPanel();

    LocalOpener(){
        super();

        this.openXml.addActionListener((ev) -> {
            readXml();
        });

        this.dandd.setBorder(BorderFactory.createEtchedBorder());
        this.dandd.add(new JLabel("<html>Drag&Drop<br/>here</html>"));

        LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(layout);

        add(this.openXml);
        add(this.dandd);

        return;
    }

    private void readXml(){

    }

}
