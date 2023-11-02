package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import shaders.StaticShader;
import skybox.SkyboxRenderer;
import terrains.Terrain;
import terrains.TerrainShader;

public class MasterRenderer {

	private static final float RED = 128 / 255f;
	private static final float GREEN = 179 / 255f;
	private static final float BLUE = 198 / 255f;

	private static final float FOV = 70;
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 1000;

	private StaticShader shader = new StaticShader();
	private EntityRenderer renderer;
	private TerrainRenderer terrainRenderer;
	private TerrainShader terrainShader = new TerrainShader();;

	private Map<TexturedModel, List<Entity>> entities = new HashMap<>();
	private List<Terrain> terrains = new ArrayList<>();
	private Matrix4f projectionMatrix;
	
	private SkyboxRenderer skyboxRenderer;

	public MasterRenderer(Loader loader) {
		enableCulling();
		createProjectionMatrix();
		renderer = new EntityRenderer(shader, projectionMatrix);
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
	}
	
	public void renderScene(List<Light> lights, Terrain[][] terrains, List<Entity> entities, Camera camera) {
		for (Terrain[] t : terrains) {
			for (Terrain terrain : t) {
				processTerrain(terrain);
			}
		}
		for (Entity ent : entities) {
			processEntity(ent);
		}
		render(lights, camera);
	}
	
	public Matrix4f getProjectionMatrix() {
		return projectionMatrix;
	}

	public void render(List<Light> lights, Camera camera) {
		sortLightsByDistanceFromPosition(lights, camera.getPosition(), 1);
		prepare();
		Vector3f skyColor = skyboxRenderer.getFinalFogColor(new Vector3f(RED, GREEN, BLUE));
		skyboxRenderer.render(camera, skyColor);
		shader.start();
		shader.loadSkyColor(skyColor);
		shader.loadViewMatrix(camera);
		shader.loadLights(lights);
		renderer.render(entities);
		shader.stop();
		terrainShader.start();
		terrainShader.loadSkyColor(skyColor);
		terrainShader.loadViewMatrix(camera);
		terrainShader.loadLights(lights);
		terrainRenderer.render(terrains);
		terrainShader.stop();
		entities.clear();
		terrains.clear();
	}

	public static void enableCulling() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}

	public static void disableCulling() {
		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(RED, GREEN, BLUE, 1);
	}

	public void processTerrain(Terrain terrain) {
		terrains.add(terrain);
	}

	public void processEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel);
		if (batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}

	public void cleanUp() {
		shader.cleanUp();
		terrainShader.cleanUp();
	}

	private void createProjectionMatrix() {
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix = new Matrix4f();
		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
	}
	
	private static List<Light> sortLightsByDistanceFromPosition(List<Light> lights, Vector3f position, int start) {
		int numLights = lights.size();
		for (int i = start; i < numLights; i++) {
			int closestLightIndex = i;
			for (int j = i + 1; j < numLights; j++) {
				if (distanceBetween(lights.get(j).getPosition(),
						position) < distanceBetween(lights.get(closestLightIndex).getPosition(), position)) {
					closestLightIndex = j;
				}
			}
			if(i != closestLightIndex) {
				lights.add(lights.get(i));
				lights.add(i, lights.remove(closestLightIndex));
				lights.remove(i + 1);
			}
		}
		return lights;
	}

	private static float distanceBetween(Vector3f position1, Vector3f position2) {
		return (float) Math.hypot(Math.hypot(position1.x - position2.x, position1.y - position2.y),
				position1.z - position2.z);
	}

}
