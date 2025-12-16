/*
 * immutable Affine transformation
 *
 * License : The MIT License
 * Copyright(c) 2009 olyutorskii
 */

package jp.sfjp.jindolf.glyph;

import java.awt.geom.AffineTransform;

/**
 * 不変のアフィン変換。
 * 恒等変換のみをサポート。
 */
@SuppressWarnings("serial")
public final class ImtblAffineTx extends AffineTransform{

    /** 恒等変換のシングルトン。 */
    public static final AffineTransform IDENTITY = new ImtblAffineTx();

    static{
        assert IDENTITY.isIdentity();
    }

    /**
     * 隠しコンストラクタ。
     * 恒等変換のみをサポート。
     */
    private ImtblAffineTx(){
        super();
        return;
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param tx {@inheritDoc}
     */
    @Override
    public void concatenate(AffineTransform tx){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param tx {@inheritDoc}
     */
    @Override
    public void preConcatenate(AffineTransform tx){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param theta {@inheritDoc}
     */
    @Override
    public void rotate(double theta){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param theta {@inheritDoc}
     * @param x {@inheritDoc}
     * @param y {@inheritDoc}
     */
    @Override
    public void rotate(double theta, double x, double y){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param sx {@inheritDoc}
     * @param sy {@inheritDoc}
     */
    @Override
    public void scale(double sx, double sy){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     */
    @Override
    public void setToIdentity(){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param theta {@inheritDoc}
     */
    @Override
    public void setToRotation(double theta){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param theta {@inheritDoc}
     * @param x {@inheritDoc}
     * @param y {@inheritDoc}
     */
    @Override
    public void setToRotation(double theta, double x, double y){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param sx {@inheritDoc}
     * @param sy {@inheritDoc}
     */
    @Override
    public void setToScale(double sx, double sy){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param shx {@inheritDoc}
     * @param shy {@inheritDoc}
     */
    @Override
    public void setToShear(double shx, double shy){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param tx {@inheritDoc}
     * @param ty {@inheritDoc}
     */
    @Override
    public void setToTranslation(double tx, double ty){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param tx {@inheritDoc}
     */
    @Override
    public void setTransform(AffineTransform tx){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param m00 {@inheritDoc}
     * @param m10 {@inheritDoc}
     * @param m01 {@inheritDoc}
     * @param m11 {@inheritDoc}
     * @param m02 {@inheritDoc}
     * @param m12 {@inheritDoc}
     */
    @Override
    public void setTransform(double m00, double m10, double m01,
                               double m11, double m02, double m12){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param shx {@inheritDoc}
     * @param shy {@inheritDoc}
     */
    @Override
    public void shear(double shx, double shy){
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * ※未サポート。
     *
     * @param tx {@inheritDoc}
     * @param ty {@inheritDoc}
     */
    @Override
    public void translate(double tx, double ty){
        throw new UnsupportedOperationException();
    }

    // TODO JRE1.6での穴を埋めるのはどうしよう…
}
