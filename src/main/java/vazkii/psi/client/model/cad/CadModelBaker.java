package vazkii.psi.client.model.cad;
import java.util.*;

/** Expands JSON cuboids and face UVs into immutable quads exactly once. */
public final class CadModelBaker {
    private CadModelBaker() {}
    public static CadBakedModel bake(CadModel model) {
        List<CadBakedModel.Quad> out=new ArrayList<CadBakedModel.Quad>();
        for(CadModel.Element e:model.elements) for(Map.Entry<String,CadModel.Face> entry:e.faces.entrySet()) {
            float[] v=vertices(entry.getKey(), e.from, e.to); if(v==null) continue;
            for(int i=0;i<12;i+=3) rotate(v,i,e.origin,e.axis,e.angle);
            float[] normal=normal(v); String layer=entry.getValue().texture.startsWith("#")?entry.getValue().texture.substring(1):entry.getValue().texture;
            float[] uv=uv(entry.getValue(),model.textureWidth,model.textureHeight); out.add(new CadBakedModel.Quad(layer,v,uv,normal));
        }
        return new CadBakedModel(out,model.transforms);
    }
    private static float[] vertices(String d,float[] f,float[] t) { float x0=f[0]/16F,y0=f[1]/16F,z0=f[2]/16F,x1=t[0]/16F,y1=t[1]/16F,z1=t[2]/16F;
        // Counter-clockwise winding when viewed from the declared outside face.
        // This matches the computed normal and keeps the textured surface on the
        // visible side when Forge/Angelica enables face culling.
        if("north".equals(d)) return new float[]{x0,y0,z0,x0,y1,z0,x1,y1,z0,x1,y0,z0}; if("south".equals(d)) return new float[]{x1,y0,z1,x1,y1,z1,x0,y1,z1,x0,y0,z1};
        if("west".equals(d)) return new float[]{x0,y0,z1,x0,y1,z1,x0,y1,z0,x0,y0,z0}; if("east".equals(d)) return new float[]{x1,y0,z0,x1,y1,z0,x1,y1,z1,x1,y0,z1};
        if("up".equals(d)) return new float[]{x0,y1,z0,x0,y1,z1,x1,y1,z1,x1,y1,z0}; if("down".equals(d)) return new float[]{x0,y0,z1,x0,y0,z0,x1,y0,z0,x1,y0,z1}; return null; }
    private static float[] uv(CadModel.Face f,float w,float h) { float u0=f.uv[0]/w,v0=f.uv[1]/h,u1=f.uv[2]/w,v1=f.uv[3]/h; float[] a=new float[]{u0,v1,u1,v1,u1,v0,u0,v0}; int r=((f.rotation%360)+360)%360/90; while(r-->0){float u=a[0],v=a[1]; for(int i=0;i<6;i+=2){a[i]=a[i+2];a[i+1]=a[i+3];}a[6]=u;a[7]=v;} return a; }
    private static void rotate(float[] p,int i,float[] origin,String axis,float degrees) { if(axis==null||degrees==0)return; float ox=origin[0]/16F,oy=origin[1]/16F,oz=origin[2]/16F,x=p[i]-ox,y=p[i+1]-oy,z=p[i+2]-oz; double r=Math.toRadians(degrees);float c=(float)Math.cos(r),s=(float)Math.sin(r); if("x".equals(axis)){p[i+1]=oy+y*c-z*s;p[i+2]=oz+y*s+z*c;}else if("y".equals(axis)){p[i]=ox+x*c+z*s;p[i+2]=oz-x*s+z*c;}else{p[i]=ox+x*c-y*s;p[i+1]=oy+x*s+y*c;} }
    private static float[] normal(float[] v) { float ax=v[3]-v[0],ay=v[4]-v[1],az=v[5]-v[2],bx=v[6]-v[0],by=v[7]-v[1],bz=v[8]-v[2];float x=ay*bz-az*by,y=az*bx-ax*bz,z=ax*by-ay*bx,l=(float)Math.sqrt(x*x+y*y+z*z);return l==0?new float[]{0,1,0}:new float[]{x/l,y/l,z/l}; }
}
