/*
 * Web-browser invoke button
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Webブラウザ起動ボタン。
 */
@SuppressWarnings("serial")
public class WebButton extends JPanel implements ActionListener{

    private static final String ACTION_SHOWWEB = "SHOWWEB";

    private final JLabel caption;
    private final JButton button;
    private String webUrlText;

    /**
     * コンストラクタ。
     */
    public WebButton(){
        super();

        this.caption = new JLabel();
        this.button = new JButton("Web");

        Monodizer.monodize(this.caption);
        this.button.setIcon(GUIUtils.getWWWIcon());
        this.button.setMargin(new Insets(1, 1, 1, 1));
        this.button.setActionCommand(ACTION_SHOWWEB);
        this.button.addActionListener(this);
        this.button.setToolTipText("Webブラウザで表示");

        design();

        return;
    }

    /**
     * レイアウトの定義。
     */
    private void design(){
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill      = GridBagConstraints.NONE;
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        constraints.insets    = new Insets(0, 0, 0, 10);

        add(this.caption, constraints);
        add(this.button,  constraints);

        return;
    }

    /**
     * {@inheritDoc}
     * @param b {@inheritDoc}
     */
    @Override
    public void setEnabled(boolean b){
        super.setEnabled(b);
        this.button.setEnabled(b);
        return;
    }

    /**
     * キャプション文字列の変更。
     * 起動URLには影響しない。
     * @param seq キャプション文字列
     */
    public void setCaption(CharSequence seq){
        this.caption.setText(seq.toString());
        return;
    }

    /**
     * Webブラウザに表示させるURLを設定する。
     * キャプション文字列も更新される。
     * @param url URL
     */
    public void setURL(URL url){
        setURLText(url.toString());
        return;
    }

    /**
     * Webブラウザに表示させるURIを設定する。
     * キャプション文字列も更新される。
     * @param uri URI
     */
    public void setURI(URI uri){
        setURLText(uri.toString());
        return;
    }

    /**
     * Webブラウザに表示させるURL文字列を設定する。
     * キャプション文字列も更新される。
     * @param urlText URL文字列
     */
    public void setURLText(CharSequence urlText){
        String str = urlText.toString();

        try{
            new URL(str);
            setEnabled(true);
        }catch(MalformedURLException e){
            setEnabled(false);
        }

        this.webUrlText = str;
        setCaption(this.webUrlText);

        return;
    }

    /**
     * WebブラウザにURLを表示させる。
     */
    public void showDialog(){
        Frame frame =
                (Frame) SwingUtilities.getAncestorOfClass(Frame.class, this);
        WebIPCDialog.showDialog(frame, this.webUrlText);
        return;
    }

    /**
     * ボタン押下イベントの受信。
     * @param event イベント
     */
    public void actionPerformed(ActionEvent event){
        if(event.getSource() != this.button) return;

        String command = event.getActionCommand();
        if(command.equals(ACTION_SHOWWEB)){
            showDialog();
        }

        return;
    }

}
