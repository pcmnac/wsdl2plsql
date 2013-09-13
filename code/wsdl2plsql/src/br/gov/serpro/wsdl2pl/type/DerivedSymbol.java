package br.gov.serpro.wsdl2pl.type;

import br.gov.serpro.wsdl2pl.parser.Context;

public abstract class DerivedSymbol extends Symbol
{
    private ElementInfo element;

    public DerivedSymbol(Context context, ElementInfo element)
    {
        super(context);
        this.element = element;
    }

    public ElementInfo getElement()
    {
        return element;
    }

    public void setElement(ElementInfo element)
    {
        this.element = element;
    }

}
