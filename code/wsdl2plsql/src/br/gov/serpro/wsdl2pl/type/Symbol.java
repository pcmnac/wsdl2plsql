package br.gov.serpro.wsdl2pl.type;

import br.gov.serpro.wsdl2pl.parser.Context;

public abstract class Symbol
{
    private Context context;

    public Symbol(Context context)
    {
        this.context = context;
    }

    public Context getContext()
    {
        return context;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

}
