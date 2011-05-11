/*
 * URI expoter
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sourceforge.jindolf;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * URIのエクスポートを行う。
 * エクスポートに使うMIMEは「text/uri-list」と「text/plain」。
 */
public class UriExporter implements Transferable{

    private static final String[] MIMES = {
        "text/uri-list",
        "text/plain",
    };
    private static final List<DataFlavor> URI_FLAVORS =
            new ArrayList<DataFlavor>(MIMES.length);

    static{
        String stringClassName = "class=" + String.class.getName();
        for(String mime : MIMES){
            String newMime = mime + "; " + stringClassName;
            DataFlavor dataFlavor;
            try{
                dataFlavor = new DataFlavor(newMime);
            }catch(ClassNotFoundException e){
                assert false;
                throw new ExceptionInInitializerError(e);
            }
            URI_FLAVORS.add(dataFlavor);
        }
    }

    private final URI uri;

    /**
     * コンストラクタ。
     * @param uri URI
     */
    public UriExporter(URI uri){
        super();
        this.uri = uri;
        return;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public DataFlavor[] getTransferDataFlavors(){
        DataFlavor[] result = new DataFlavor[URI_FLAVORS.size()];
        int index = 0;
        for(DataFlavor dataFlavor : URI_FLAVORS){
            try{
                result[index++] = (DataFlavor)( dataFlavor.clone() );
            }catch(CloneNotSupportedException e){
                assert false;
                throw new AssertionError(e);
            }
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * @param flavor {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor){
        for(DataFlavor dataFlavor : URI_FLAVORS){
            if(dataFlavor.equals(flavor)) return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * エクスポートするURI文字列を返す。
     * @param flavor {@inheritDoc}
     * @return {@inheritDoc}
     * @throws java.awt.datatransfer.UnsupportedFlavorException {@inheritDoc}
     * @throws java.io.IOException {@inheritDoc}
     */
    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException,
                   IOException {
        if( ! isDataFlavorSupported(flavor) ){
            throw new UnsupportedFlavorException(flavor);
        }

        String result = this.uri.toASCIIString();

        return result;
    }

}
