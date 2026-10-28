import java.io.*;
import java.util.*;
/** Report serialization structure through a filtered input stream. */
public class InspectDatastore {
  static class InspectInput extends ObjectInputStream {
    long arrays=0,bytes=0;
    Map<String,Long> types=new TreeMap<>();
    InspectInput(InputStream in) throws IOException {super(in); enableResolveObject(true);}
    protected Object resolveObject(Object x) {
      if(x!=null)types.merge(x.getClass().getName(),1L,Long::sum);
      if(x instanceof byte[] b) {
        arrays++;bytes+=b.length;
        if(arrays<=3)System.out.println("BYTE_ARRAY_SAMPLE length="+b.length+" prefix="+HexFormat.of().formatHex(Arrays.copyOf(b,Math.min(b.length,100))));
      }
      return x;
    }
  }
  public static void main(String[] args) throws Exception {
    try(var in=new InspectInput(new FileInputStream(args[0]))) {
      in.setObjectInputFilter(info -> {
        var c=info.serialClass();
        if(info.depth()>1000 || info.references()>100000000 || info.arrayLength()>200000000) return ObjectInputFilter.Status.REJECTED;
        if(c==null || c.isPrimitive() || c.isArray() || c.getName().startsWith("java.util.") || c.getName().startsWith("java.lang.")) return ObjectInputFilter.Status.ALLOWED;
        return ObjectInputFilter.Status.REJECTED;
      });
      System.out.println("DATASTORE_VERSION="+in.readLong());
      try {
        Object root=in.readObject();
        System.out.println("DATASTORE_STATUS=complete_top_level_object");
        if(root instanceof Map<?,?> m)System.out.println("ROOT_KEYS="+m.keySet());
      } catch(EOFException e) {
        System.out.println("DATASTORE_STATUS=incomplete_serialized_stream");
        System.out.println("The stream ended before its top-level object was complete.");
      }
      System.out.println("COMPLETED_OBJECT_TYPES="+in.types);
      System.out.println("COMPLETED_BYTE_ARRAYS="+in.arrays+" TOTAL_BYTES="+in.bytes);
    }
  }
}
