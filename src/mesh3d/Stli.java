package mesh3d;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;

import math.geom3d.Point3D;

public class Stli {
	private static Tri3D[] importMesh(String fileName,boolean ascii) throws IOException{
		try{
			if(!ascii){
				throw new InputMismatchException("Please input ASCII .stl files only");
			}
			BufferedReader f = new BufferedReader(new FileReader(fileName));
			ArrayList<Tri3D> tris = new ArrayList<Tri3D>();
			Point3D[] ps = new Point3D[]{Constants.origin,Constants.origin,Constants.origin};
			int i=0;
			boolean inFace = false;
			while(f.ready()){
				String[] lineArray = f.readLine().trim().split(" ");
				switch(lineArray[0]){
				case "facet":
					if(inFace){
						throw new IllegalArgumentException("Invalid .stl file.");
					}
					inFace = true;
					break;
				case "vertex":
					if(!inFace){
						throw new IllegalArgumentException("Invalid .stl file.");
					}
					ps[i] = new Point3D(Double.parseDouble(lineArray[1]),Double.parseDouble(lineArray[2]),Double.parseDouble(lineArray[3]));
					i++;
					break;
				case "endfacet":
					if(!inFace||i!=3){
						throw new IllegalArgumentException("Invalid .stl file.");
					}
					tris.add(new Tri3D(ps[0],ps[1],ps[2]));
					ps = new Point3D[]{Constants.origin,Constants.origin,Constants.origin};
					i=0;
					inFace = false;
					break;
				default:
					//Do nothing in other cases.
					break;
				}
			}
			f.close();
			return tris.toArray(new Tri3D[tris.size()]);
		} catch(NumberFormatException e){
			e.printStackTrace();
			return null;
		}
	}
	public static Model3D importModel(String fileName, boolean ascii) throws IOException{
		return new Model3D(importMesh(fileName,ascii));
	}
	public static Surface3D importSurface(String fileName, boolean ascii) throws IOException{
		return new Surface3D(importMesh(fileName,ascii));
	}
}
