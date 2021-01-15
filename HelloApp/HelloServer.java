package HelloApp;

import java.util.Properties;

public class HelloServer {

  public static void main(String[] args) {

    Properties prop = new Properties();
    prop.put("org.omg.CORBA.ORBInitialHost", "localhost");
    prop.put("org.omg.CORBA.ORBInitialPort", "1050");
  
    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, prop);
    org.omg.CORBA.Object obj = null;
    org.omg.PortableServer.POA rootPOA = null;

    try {
      obj = orb.resolve_initial_references("RootPOA");
    } catch (org.omg.CORBA.ORBPackage.InvalidName e){
      System.out.println("No service with name: " + e.getMessage());
    }

    rootPOA = org.omg.PortableServer.POAHelper.narrow(obj);
    HelloServant hello = new HelloServant();
    org.omg.CosNaming.NamingContextExt rootContext = null;

    try {
      byte[] servantId = rootPOA.activate_object(hello);
      org.omg.CORBA.Object ref = rootPOA.id_to_reference(servantId);
      org.omg.CORBA.Object objRef = null;

      try {
        objRef = orb.resolve_initial_references("NameService");
        System.out.println("Finding naming service...");
        rootContext = org.omg.CosNaming.NamingContextExtHelper.narrow(objRef);
        System.out.println("Naming Service Found!!!");
      } catch (org.omg.CORBA.ORBPackage.InvalidName name) {
        System.out.println("Invalid service name...");
        name.printStackTrace();
        System.exit(0);
      }
      String text = "hello";
      org.omg.CosNaming.NameComponent[] path = rootContext.to_name(text);
      try {
        rootContext.bind(path, ref);
      } catch (org.omg.CosNaming.NamingContextPackage.NotFound e) {
        System.out.println("Object not found...");
        System.exit(0);
      } catch (org.omg.CosNaming.NamingContextPackage.AlreadyBound e) {
        System.out.println("Object already exists...");
        rootContext.rebind(path, ref);
        System.out.println("Rebinding object...");
      } catch (org.omg.CosNaming.NamingContextPackage.CannotProceed e) {
        System.out.println("Error. Server cannot proceed...");
        System.exit(0);
      }
      rootPOA.the_POAManager().activate();
      System.out.println("Java Server active and waiting...");
      orb.run();

    } catch (java.lang.Exception e) {
      System.out.println("There was a problem with the server...\n" + e.getMessage());
    }
  }
}
