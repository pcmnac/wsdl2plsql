package br.gov.serpro.wsdl2pl.writer;

import br.gov.serpro.wsdl2pl.Context;

public class BaseWriter
{

    protected static final int BODY_INDENT = 1;

    private Context context;

    protected static final int INDENT = 1;

    public BaseWriter(Context context)
    {
        this.context = context;
    }

    protected Context getContext()
    {
        return context;
    }

}
