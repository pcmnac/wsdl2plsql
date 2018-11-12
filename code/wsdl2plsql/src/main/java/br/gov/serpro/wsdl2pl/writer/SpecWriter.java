package br.gov.serpro.wsdl2pl.writer;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.type.Exception;
import br.gov.serpro.wsdl2pl.type.Function;
import br.gov.serpro.wsdl2pl.type.Type;
import br.gov.serpro.wsdl2pl.util.SB;

public class SpecWriter extends BaseWriter
{
    public SpecWriter(Context context)
    {
        super(context);
    }

    public String write()
    {
        SB spec = new SB();
        IKeywordEmitter ke = getContext().getKeywordEmitter();

        // TODO: tratar
        String packageName = getContext().getPackageName();

        // CREATE OR REPLACE PACKAGE packageName AS
        spec.l("%s %s %s %s\n", ke.createOrReplace(), ke.packageKey(), packageName, ke.as());

        // TODO: tratar
        spec.l(1, "-- TYPES\n");

        for (Type plType : getContext().getCustomTypes())
        {
            if (getContext().isElegible(plType))
            {
                spec.l(1, "-- " + plType.comments());
                spec.l(1, plType.forwardDecl() + "\n");
            }
        }

        for (Type plType : getContext().getCustomTypes())
        {
            if (getContext().isElegible(plType))
            {
                spec.l(1, "-- " + plType.comments());
                spec.l(plType.decl(1));
            }
        }

        spec.l(1, "-- EXCEPTIONS\n");

        for (Exception exception : getContext().getExceptions())
        {
            if (getContext().isElegible(exception))
            {
                spec.l(1, "-- %s", exception.comments());
                spec.l(1, "%s;", exception.decl());
                if (exception.getType() != null || exception.getNumber() != null)
                {
                    if (exception.getType() != null)
                    {
                        // err_name EXCEPTION;
                        spec.l(1, "%s;\n", exception.var());
                    }

                    if (exception.getNumber() != null)
                    {
                        // PRAGMA EXCEPTION_INIT(err_name, -20000);
                        spec.l(1, "%s %s(%s, %d);\n", ke.pragma(), ke.exceptionInit(), exception.name(),
                                exception.getNumber());
                    }
                }
                else
                {
                    spec.l();
                }
            }
        }

        // TODO: tratar
        spec.l(1, "-- FUNCTIONS\n");

        if (!getContext().getPlFunctions().isEmpty())
        {
            for (Function function : getContext().getPlFunctions())
            {
                if (getContext().isElegible(function))
                {
                    spec.a(function.doc(BODY_INDENT));
                    spec.l(function.decl(BODY_INDENT, true) + ";\n");
                }
            }
        }

        // END packageName;
        spec.l(String.format("%s %s;", ke.end(), packageName));

        return spec.toString();
    }

}
