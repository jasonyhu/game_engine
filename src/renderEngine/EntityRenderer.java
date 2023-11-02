package renderEngine;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;

import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import shaders.StaticShader;
import textures.ModelTexture;
import toolbox.GaMath;

public class EntityRenderer {

	private StaticShader shader;
	
	public EntityRenderer(StaticShader shader, Matrix4f projectionMatrix) {
		this.shader = shader;
		shader.start();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
	}

	public void render(Map<TexturedModel, List<Entity>> entities) {
		for (TexturedModel model : entities.keySet()) {
			prepareTexturedModel(model);
			List<Entity> batch = entities.get(model);
			for(Entity entity : batch) {
				prepareInstance(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, model.getRawModel().getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
			unbindTexturedModel();
		}
	}
	
	private void prepareTexturedModel(TexturedModel texturedModel) {
		RawModel model = texturedModel.getRawModel();
		GL30.glBindVertexArray(model.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		ModelTexture texture = texturedModel.getTexture();
		shader.loadNumberOfRows(texture.getNumberOfRows());
		if (texture.hasTransparency()) {
			MasterRenderer.disableCulling();
		}
		shader.loadFakeLightingVariable(texture.useFakeLighting());
		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getID());
	}
	
	private void unbindTexturedModel() {
		MasterRenderer.enableCulling();
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
	}
	
	private void prepareInstance(Entity entity) {
		Matrix4f transformationMatrix = GaMath.createTransformationMatrix(entity.getPosition(), entity.getRotX(),
				entity.getRotY(), entity.getRotZ(), entity.getScale());
		shader.loadTransformationMatrix(transformationMatrix);
		shader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
	}

	// old method
//	public void render(Entity entity, StaticShader shader) {
//		TexturedModel texturedModel = entity.getModel();
//		RawModel model = texturedModel.getRawModel();
//		GL30.glBindVertexArray(model.getVaoID());
//		GL20.glEnableVertexAttribArray(0);
//		GL20.glEnableVertexAttribArray(1);
//		GL20.glEnableVertexAttribArray(2);
//		Matrix4f transformationMatrix = GaMath.createTransformationMatrix(entity.getPosition(), entity.getRotX(),
//				entity.getRotY(), entity.getRotZ(), entity.getScale());
//		shader.loadTransformationMatrix(transformationMatrix);
//		ModelTexture texture = texturedModel.getTexture();
//		shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
//		GL13.glActiveTexture(GL13.GL_TEXTURE0);
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texturedModel.getTexture().getID());
//		GL11.glDrawElements(GL11.GL_TRIANGLES, model.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
//		GL20.glDisableVertexAttribArray(0);
//		GL20.glDisableVertexAttribArray(1);
//		GL20.glDisableVertexAttribArray(2);
//		GL30.glBindVertexArray(0);
//	}

}
