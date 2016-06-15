package br.gov.serpro.wsdl2pl.type;

import br.gov.serpro.wsdl2pl.Context;

public abstract class Type extends Symbol implements Identifiable<String>
{

    public Type(Context context)
    {
        super(context);
    }

    public abstract String decl(int indent);

    public abstract String forwardDecl();

    public abstract String comments();
}
