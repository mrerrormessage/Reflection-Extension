import org.nlogo.api.Argument;
import org.nlogo.api.ClassManager;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.ExtensionManager;
import org.nlogo.api.ExtensionObject;
import org.nlogo.api.ImportErrorHandler;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.PrimitiveManager;
import org.nlogo.api.Syntax;
import org.nlogo.app.App;
import org.nlogo.headless.HeadlessWorkspace;
import org.nlogo.nvm.Activation;
import org.nlogo.nvm.Procedure;
import org.nlogo.nvm.ExtensionContext;

import java.util.List;
import java.util.Map;

/**
 * Created by hah661 on 5/14/15.
 */
public class NetLogoReflection implements ClassManager {

    public void runOnce(ExtensionManager em) {
    }

    public void load(PrimitiveManager primitiveManager) {
        primitiveManager.addPrimitive("breeds", new Breeds());
        primitiveManager.addPrimitive("globals", new Globals());
        primitiveManager.addPrimitive("procedures", new Procedures());
        primitiveManager.addPrimitive("current-procedure", new CurrentProcedure());
        primitiveManager.addPrimitive("callers", new Callers());
    }

    public static class Globals extends DefaultReporter {
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(
                    // we take in int[] {modelNumber, varName}
                    new int[] { Syntax.NumberType() },
                    // and return a number
                    Syntax.ListType());
        }

        public Object report(Argument args[], Context context)
                throws ExtensionException, org.nlogo.api.LogoException {
            LogoListBuilder llb = new LogoListBuilder();

            for (int i = 0; i < App.app().workspace().world().observer().getVariableCount(); i++){
                llb.add(App.app().workspace().world().observer().variableName(i));
            }
            return llb.toLogoList();

        }
    }

    public static class Procedures extends DefaultReporter {
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(
                    // we take in int[] {modelNumber, varName}
                    new int[] { },
                    // and return a number
                    Syntax.ListType());
        }

        public Object report(Argument args[], Context context)
                throws ExtensionException, org.nlogo.api.LogoException {
            LogoListBuilder outerLLB = new LogoListBuilder();
            for (String pName : App.app().workspace().getProcedures().keySet()){
                LogoListBuilder pList = new LogoListBuilder();
                Procedure p = App.app().workspace().getProcedures().get(pName);
                pList.add(pName);
                pList.add(p.tyype.toString());
                pList.add(p.usableBy);
                LogoListBuilder argLLB = new LogoListBuilder();
                // args contains dummies (temp 'lets') so we don't include them.
                // localsCount contains number of lets so we just subtract that
                for (int i = 0; i < p.args.size() - p.localsCount;i++){
                    String theString = p.args.get(i);
                    argLLB.add(theString);
                }
                pList.add(argLLB.toLogoList());
//            pList.add(org.nlogo.api.Synp.syntax().ret()) find names here
                outerLLB.add(pList.toLogoList());
            }
            return outerLLB.toLogoList();
        }
    }

    public static class CurrentProcedure extends DefaultReporter {
      public Syntax getSyntax() {
        return Syntax.reporterSyntax(new int[] {}, Syntax.StringType());
      }

      public Object report(Argument args[], Context context)
        throws ExtensionException, org.nlogo.api.LogoException {
        return ((ExtensionContext) context).nvmContext().activation.procedure.name;
      }
    }

    public static class Callers extends DefaultReporter {
      public Syntax getSyntax() {
        return Syntax.reporterSyntax(new int[] {}, Syntax.ListType());
      }

      public Object report(Argument args[], Context context)
        throws ExtensionException, org.nlogo.api.LogoException {
        Activation activation = ((ExtensionContext) context).nvmContext().activation;
        LogoListBuilder llb = new LogoListBuilder();
        while (activation != null && activation.procedure != null) {
          llb.add(activation.procedure.name);
          activation = activation.parent;
        }
        return llb.toLogoList();
      }
    }

    public static class Breeds extends DefaultReporter {
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(
                    // we take in int[] {modelNumber, varName}
                    new int[] { Syntax.NumberType() },
                    // and return a number
                    Syntax.ListType());
        }

        public Object report(Argument args[], Context context)
                throws ExtensionException, org.nlogo.api.LogoException {
            LogoListBuilder llb = new LogoListBuilder();
            // add turtle vars as a separate tuple
            LogoListBuilder tuple  = new LogoListBuilder();
            LogoListBuilder vars = new LogoListBuilder();
            for (String s: App.app().workspace().world().program().turtlesOwn()){
                vars.add(s);
            }
            tuple.add("TURTLES");
            tuple.add(vars.toLogoList());
            llb.add(tuple.toLogoList());

            for (Map.Entry<String, List<String>> entry : App.app().workspace().world().program().breedsOwn().entrySet())
            {
                tuple  = new LogoListBuilder();
                vars = new LogoListBuilder();
                for (Object o : entry.getValue()){
                    String s = (String)o;
                    vars.add(o);
                }
                tuple.add(entry.getKey());
                tuple.add(vars.toLogoList());
                llb.add(tuple.toLogoList());
            }
            return llb.toLogoList();

        }
    }

    public void unload(ExtensionManager em) {
    }

    public ExtensionObject readExtensionObject(ExtensionManager reader, Object typeName, Object value) {
        return null;
    }

    public StringBuilder exportWorld() {
        return null;
    }

    public void importWorld(List<String[]> lines, ExtensionManager reader, ImportErrorHandler handler) {
    }

    public void clearAll() {
    }

    public ExtensionObject readExtensionObject(ExtensionManager reader, String typeName, String value){
        return null;
    }

    public List<String> additionalJars() {
        return null;
    }
}
