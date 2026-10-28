import java.io.*;
import java.util.*;
public class InspectDatastore {
  static void walk(Object x, String path, int depth) {
    System.out.println(path + " type=" + (x == null ? "null" : x.getClass().getName()));
    if (x instanceof Map<?,?> m) {
      System.out.println(path + " size=" + m.size());
      int n=0;
      for(var e:m.entrySet()) {
        if(n++>=8) break;
        walk(e.getValue(), path + "/" + e.getKey(), depth+1);
      }
    } else if(x instanceof byte[] b) {
      System.out.println(path+" bytes="+b.length+" prefix="+HexFormat.of().formatHex(Arrays.copyOf(b,Math.min(b.length,120))));
    } else if(x instanceof Collection<?> c) {
      System.out.println(path+" count="+c.size());
      int n=0; for(var v:c) {if(n++>=2)break;walk(v,path+"/"+n,depth+1);}
    } else if(x != null) System.out.println(path+" value="+x.toString().substring(0,Math.min(x.toString().length(),100)));
  }
  public static void main(String[] args) throws Exception {
    try(var in=new ObjectInputStream(new FileInputStream(args[0]))) {
      in.setObjectInputFilter(info -> {
        var c=info.serialClass();
        if(info.depth()>100 || info.references()>10000000 || info.arrayLength()>200000000) return ObjectInputFilter.Status.REJECTED;
        if(c==null || c.isPrimitive() || c.isArray() || c.getName().startsWith("java.util.") || c.getName().startsWith("java.lang.")) return ObjectInputFilter.Status.ALLOWED;
        return ObjectInputFilter.Status.REJECTED;
      });
      System.out.println("VERSION="+in.readLong());
      walk(in.readObject(),"root",0);
    }
  }
}
