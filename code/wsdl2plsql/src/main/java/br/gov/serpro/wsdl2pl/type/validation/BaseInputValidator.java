package br.gov.serpro.wsdl2pl.type.validation;

import br.gov.serpro.wsdl2pl.Context;

public abstract class BaseInputValidator implements InputValidator
{
    private Context context;

    public BaseInputValidator(Context context)
    {
        this.context = context;
    }

    protected Context getContext()
    {
        return this.context;
    }

}
