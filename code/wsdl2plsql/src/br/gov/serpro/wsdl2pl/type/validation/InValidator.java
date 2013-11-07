package br.gov.serpro.wsdl2pl.type.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.util.SB;

public class InValidator extends BaseInputValidator
{
    public InValidator(Context context)
    {
        super(context);
    }

    private List<String> validValues = new ArrayList<String>();

    @Override
    public String emit(int indent, String varName)
    {
        SB snippet = new SB();

        IKeywordEmitter ke = getContext().getKeywordEmitter();

        // IF value NOT IN (...) THEN
        snippet.a(indent, "%s %s %s %s (", ke.ifKey(), varName, ke.not(), ke.in());
        for (int i = 0; i < validValues.size(); i++)
        {
            snippet.a(validValues.get(i));
            if (i < validValues.size() - 1)
            {
                snippet.a(", ");
            }
        }
        snippet.l(") %s", ke.then());

        snippet.l(indent + 1, "%s(%s, '%s');", ke.raiseApplicationError(), getContext().getInputValidationException()
                .getNumber(), errorMessage(varName));

        // END IF:
        snippet.l(indent, "%s %s;", ke.end(), ke.ifKey());

        return snippet.toString();
    }

    public void addValidValue(String value)
    {
        validValues.add(value);
    }

    @Override
    public String randomValue()
    {
        String result = "abcdef";

        if (!validValues.isEmpty())
        {
            result = validValues.get((int) (Math.random() * validValues.size()));
        }

        return result;
    }

    @Override
    public String errorMessage(String varName)
    {
        return String.format("\"' || %s || '\" is not a valid value for %s. Valid values: %s.", varName, varName,
                validValues());
    }

    public String validValues()
    {
        return Arrays.toString(validValues.toArray()).replaceAll("'", "''");
    }

}
