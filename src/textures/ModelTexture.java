package textures;

public class ModelTexture {
	
	private int textureID;
	
	private float shineDamper = 1;
	private float reflectivity = 0;
	
	private boolean hasTransparency = false;
	private boolean useFakeLighting = false;
	
	private int numberOfRows = 1;

	public float getShineDamper() {
		return shineDamper;
	}

	public ModelTexture setShineDamper(float shineDamper) {
		this.shineDamper = shineDamper;
		return this;
	}
	
	public int getNumberOfRows() {
		return numberOfRows;
	}

	public void setNumberOfRows(int numberOfRows) {
		this.numberOfRows = numberOfRows;
	}

	public float getReflectivity() {
		return reflectivity;
	}

	public ModelTexture setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
		return this;
	}

	public ModelTexture(int id) {
		this.textureID = id;
	}
	
	public boolean hasTransparency() {
		return hasTransparency;
	}
	
	public void setHasTransparency(boolean hasTransparency) {
		this.hasTransparency = hasTransparency;
	}
	
	public boolean useFakeLighting() {
		return useFakeLighting;
	}
	
	public void setUseFakeLighting(boolean useFakeLighting) {
		this.useFakeLighting = useFakeLighting;
	}

	public int getID() {
		return this.textureID;
	}
}
