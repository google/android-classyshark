# classyshark-sample-plugin
How to use [ClassyShark] (https://github.com/google/android-classyshark) in your build/continous integration and call its APIs. 

``` Java

public class Shark {

   /**
     * @param className class name to generate such as "com.bumptech.glide.request.target.BaseTarget"
     * @return a new Shark instance
     */
   public static Shark with(File archiveFile);

    /**
     * @param className class name to generate such as "com.bumptech.glide.request.target.BaseTarget"
     * @return
     */
    public String getGeneratedClass(String className);
       
    /**
     * @return list of class names
     */
    public List<String> getAllClassNames();
    
    /**
     * @return manifest
     */
    public String getManifest();

    /**
     * @return all methods
     */
    public List<String> getAllMethods();
    /**
     * @return all strings from all string tables
     */
    public List<String> getAllStrings();
}

```
