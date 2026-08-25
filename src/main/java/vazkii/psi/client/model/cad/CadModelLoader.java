package vazkii.psi.client.model.cad;

import java.io.InputStreamReader;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/** Loads cad.json once; no resource or JSON work happens in the render path. */
public final class CadModelLoader {
    private CadModelLoader() {}
    public static CadModel load(ResourceLocation location) {
        try {
            JsonObject root = new JsonParser().parse(new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream(), "UTF-8")).getAsJsonObject();
            Map<String, String> textures = new HashMap<String, String>();
            for (Map.Entry<String, JsonElement> e : root.getAsJsonObject("textures").entrySet()) textures.put(e.getKey(), e.getValue().getAsString());
            Map<String, CadModel.Transform> transforms = new HashMap<String, CadModel.Transform>();
            JsonObject display = root.getAsJsonObject("display");
            if (display != null) for (Map.Entry<String, JsonElement> e : display.entrySet()) { JsonObject v=e.getValue().getAsJsonObject(); transforms.put(e.getKey(), new CadModel.Transform(array(v,"rotation",0F), array(v,"translation",0F), array(v,"scale",1F))); }
            List<CadModel.Element> elements = new ArrayList<CadModel.Element>();
            for (JsonElement value : root.getAsJsonArray("elements")) {
                JsonObject e=value.getAsJsonObject(); String axis=null; float angle=0F; float[] origin=new float[]{0,0,0};
                if (e.has("rotation")) { JsonObject r=e.getAsJsonObject("rotation"); axis=r.get("axis").getAsString(); angle=r.get("angle").getAsFloat(); origin=array(r,"origin",0F); }
                Map<String,CadModel.Face> faces=new LinkedHashMap<String,CadModel.Face>();
                for(Map.Entry<String,JsonElement> f:e.getAsJsonObject("faces").entrySet()) { JsonObject x=f.getValue().getAsJsonObject(); faces.put(f.getKey(),new CadModel.Face(array(x,"uv",0F),x.get("texture").getAsString(),x.has("rotation")?x.get("rotation").getAsInt():0,x.has("cullface")?x.get("cullface").getAsString():null)); }
                elements.add(new CadModel.Element(e.has("name")?e.get("name").getAsString():"",array(e,"from",0F),array(e,"to",0F),origin,axis,angle,faces));
            }
            float[] size=array(root,"texture_size",32F); return new CadModel(elements,textures,transforms,size[0],size[1]);
        } catch (Exception e) { throw new IllegalStateException("Unable to load CAD model " + location, e); }
    }
    private static float[] array(JsonObject o,String key,float fallback) {
        // JSON model vectors are triples, but face UVs are quads and texture_size
        // is a pair. Retaining each shape prevents lossy baking of per-face UVs.
        int length = "uv".equals(key) ? 4 : "texture_size".equals(key) ? 2 : 3;
        float[] a=new float[length]; Arrays.fill(a,fallback); if(!o.has(key)) return a;
        JsonArray in=o.getAsJsonArray(key); for(int i=0;i<in.size()&&i<length;i++)a[i]=in.get(i).getAsFloat(); return a;
    }
}
