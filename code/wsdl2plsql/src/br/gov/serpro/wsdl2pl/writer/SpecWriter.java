package br.gov.serpro.wsdl2pl.writer;

import br.gov.serpro.wsdl2pl.Context;
import br.gov.serpro.wsdl2pl.emitter.IKeywordEmitter;
import br.gov.serpro.wsdl2pl.type.Exception;
import br.gov.serpro.wsdl2pl.type.Function;
import br.gov.serpro.wsdl2pl.type.Parameter;
import br.gov.serpro.wsdl2pl.type.Type;
import br.gov.serpro.wsdl2pl.util.SB;

public class SpecWriter extends BaseWriter
{
    private Context context;

    public SpecWriter(Context context)
    {
        this.context = context;
    }

    public String writeSpec()
    {
        SB spec = new SB();
        IKeywordEmitter ke = context.getKeywordEmitter();

        // TODO: tratar
        String packageName = context.getPackageName();

        // CREATE OR REPLACE PACKAGE packageName AS
        spec.l("%s %s %s %s\n", ke.createOrReplace(), ke.packageKey(), packageName, ke.as());

        // TODO: tratar
        spec.l(1, "-- TYPES\n");

        for (Type plType : context.getComplexTypeMap().values())
        {
            spec.l(1, "-- " + plType.comments());
            spec.l(1, plType.forwardDecl() + "\n");
        }

        for (Type plType : context.getComplexTypeMap().values())
        {
            spec.l(1, "-- " + plType.comments());
            spec.l(plType.decl(1));
        }

        spec.l(1, "-- EXCEPTIONS\n");

        for (Exception exception : context.getExceptions())
        {
            spec.l(1, "-- %s", exception.comments());
            spec.l(1, "%s;", exception.decl());
            spec.l(1, "%s;\n", exception.var());
        }

        // TODO: tratar
        spec.l(1, "-- FUNCTIONS\n");

        if (!context.getPlFunctions().isEmpty())
        {
            for (Function function : context.getPlFunctions())
            {
                spec.l(BODY_INDENT, "/**");
                spec.l(BODY_INDENT, " * " + function.comments());
                spec.l(BODY_INDENT, " * ");
                spec.l(BODY_INDENT, " * @author wsdl2plsql (generated)");
                spec.l(BODY_INDENT, " * ");

                for (Parameter parameter : function.getParameters())
                {
                    spec.l(BODY_INDENT, " * @param %s %s", parameter.name(), parameter.getElement());
                }
                if (function.getInputHeader() != null)
                {
                    spec.l(BODY_INDENT, " * @param %s %s", function.getInputHeader().name(), function.getInputHeader()
                            .getElement());
                }
                if (function.getOutputHeader() != null)
                {
                    spec.l(BODY_INDENT, " * @param %s %s", function.getOutputHeader().name(), function
                            .getOutputHeader().getElement());
                }
                spec.l(BODY_INDENT, " * @param %s %s", function.getUrlParam().name(), "Service URL");

                spec.l(BODY_INDENT, " * ");
                spec.l(BODY_INDENT, " * @return %s", function.getReturnElement());
                spec.l(BODY_INDENT, " * ");

                for (Exception exception : function.getExceptions())
                {
                    spec.l(BODY_INDENT, " * @throws " + exception.name());
                }

                spec.l(BODY_INDENT, " * @throws " + context.getSoapFaultException().name());
                spec.l(BODY_INDENT, " * ");
                spec.l(BODY_INDENT, " */ ");
                spec.l(function.decl(BODY_INDENT) + ";\n");
            }
        }

        // END packageName;
        spec.l(String.format("%s %s;", ke.end(), packageName));

        return spec.toString();
    }

}
