package com.apress.ba3tp.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

/**
 * This class is an object representation of 
 * a Cube containing the vertex information,
 * texture coordinates, the vertex indices
 * and drawing functionality, which is called 
 * by the renderer.
 *  
 * @author Savas Ziplies (nea/INsanityDesign)
 */
public class Cube {
  private Context mContext;
  
	/** The buffer holding the vertices */
	private FloatBuffer vertexBuffer;
	/** The buffer holding the texture coordinates */
	private FloatBuffer textureBuffer;
	/** The buffer holding the indices */
	private ByteBuffer indexBuffer;
	/** The buffer holding the normals */
	private FloatBuffer normalBuffer;

	/** Our texture pointer */
	private int[] textures = new int[2];

	/** The initial vertex definition */	
	private float vertices[] = {
						//Vertices according to faces
	    // Drawn as triangles.
						-1.0f, -1.0f, 1.0f, //v0
						1.0f, -1.0f, 1.0f, 	//v1
						-1.0f, 1.0f, 1.0f, 	//v2
						1.0f, 1.0f, 1.0f, 	//v3
			
						1.0f, -1.0f, 1.0f, 
						1.0f, -1.0f, -1.0f, 
						1.0f, 1.0f, 1.0f, 
						1.0f, 1.0f, -1.0f,
			
						1.0f, -1.0f, -1.0f, 
						-1.0f, -1.0f, -1.0f, 
						1.0f, 1.0f, -1.0f, 
						-1.0f, 1.0f, -1.0f,
			
						-1.0f, -1.0f, -1.0f, 
						-1.0f, -1.0f, 1.0f, 
						-1.0f, 1.0f, -1.0f, 
						-1.0f, 1.0f, 1.0f,
			
						-1.0f, -1.0f, -1.0f, 
						1.0f, -1.0f, -1.0f, 
						-1.0f, -1.0f, 1.0f, 
						1.0f, -1.0f, 1.0f,
			
						-1.0f, 1.0f, 1.0f, 
						1.0f, 1.0f, 1.0f, 
						-1.0f, 1.0f, -1.0f, 
						1.0f, 1.0f, -1.0f, 
											};

	/** 
	 * The initial normals for the lighting calculations.
	 * These have only been roughly calculated, and are not guaranteed to be correct. 
	 */	
	private float normals[] = {
						// Normals
						0.0f, 0.0f, 1.0f, 						
						0.0f, 0.0f, -1.0f, 
						0.0f, 1.0f, 0.0f, 
						0.0f, -1.0f, 0.0f, 
						
						0.0f, 0.0f, 1.0f, 
						0.0f, 0.0f, -1.0f, 
						0.0f, 1.0f, 0.0f, 
						0.0f, -1.0f, 0.0f,
						
						0.0f, 0.0f, 1.0f, 
						0.0f, 0.0f, -1.0f, 
						0.0f, 1.0f, 0.0f, 
						0.0f, -1.0f, 0.0f,
						
						0.0f, 0.0f, 1.0f, 
						0.0f, 0.0f, -1.0f, 
						0.0f, 1.0f, 0.0f, 
						0.0f, -1.0f, 0.0f,
						
						0.0f, 0.0f, 1.0f, 
						0.0f, 0.0f, -1.0f, 
						0.0f, 1.0f, 0.0f, 
						0.0f, -1.0f, 0.0f,
						
						0.0f, 0.0f, 1.0f, 
						0.0f, 0.0f, -1.0f, 
						0.0f, 1.0f, 0.0f, 
						0.0f, -1.0f, 0.0f};

	/** The initial texture coordinates (u, v) */	
	private float texture[] = {
						//Mapping coordinates for the vertices
						0.0f, 0.0f, 
						0.0f, 1.0f, 
						1.0f, 0.0f, 
						1.0f, 1.0f,
			
						0.0f, 0.0f,
						0.0f, 1.0f, 
						1.0f, 0.0f,
						1.0f, 1.0f,
			
						0.0f, 0.0f, 
						0.0f, 1.0f, 
						1.0f, 0.0f, 
						1.0f, 1.0f,
			
						0.0f, 0.0f, 
						0.0f, 1.0f, 
						1.0f, 0.0f, 
						1.0f, 1.0f,
			
						0.0f, 0.0f, 
						0.0f, 1.0f, 
						1.0f, 0.0f, 
						1.0f, 1.0f,
			
						0.0f, 0.0f, 
						0.0f, 1.0f, 
						1.0f, 0.0f, 
						1.0f, 1.0f};

	/** The initial indices definition */
	private byte indices[] = {
						// Faces definition
						0, 1, 3, 0, 3, 2, 		// Face front
						4, 5, 7, 4, 7, 6, 		// Face right
						8, 9, 11, 8, 11, 10, 	// ...
						12, 13, 15, 12, 15, 14, 
						16, 17, 19, 16, 19, 18, 
						20, 21, 23, 20, 23, 22};

	public Cube() {
		//
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		//
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);

		//
		byteBuf = ByteBuffer.allocateDirect(normals.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		normalBuffer = byteBuf.asFloatBuffer();
		normalBuffer.put(normals);
		normalBuffer.position(0);

		//
		indexBuffer = ByteBuffer.allocateDirect(indices.length);
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}

	public void draw(GL10 gl) {

		//Enable the vertex, texture and normal state
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);

		//Set the face rotation
		gl.glFrontFace(GL10.GL_CCW);
		
		//Point to our buffers
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		gl.glNormalPointer(GL10.GL_FLOAT, 0, normalBuffer);
		
		//Draw the vertices as triangles, based on the Index Buffer information
		indexBuffer.position(0);
    //Bind the texture according to the set texture filter
    gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_BYTE, indexBuffer); //Draw front.
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[1]); // Get the side
    indexBuffer.position(6);
    gl.glDrawElements(GL10.GL_TRIANGLES, indices.length-6, GL10.GL_UNSIGNED_BYTE, indexBuffer); //Draw everything else.
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
	}

		public Bitmap loadBitmap(int resourceId) {
    InputStream is = mContext.getResources().openRawResource(resourceId);
    Bitmap bitmap = null;
    try {
      bitmap = BitmapFactory.decodeStream(is);
    } finally {
      //Always clear and close
      try {
        is.close();
        is = null;
      } catch (IOException e) {
      }
    }
    return bitmap;
	}
	
	public void loadGLTexture(GL10 gl, Context context) {
	  mContext = context;
	  Bitmap bitmap = loadBitmap(R.drawable.safefront);
		//Generate two texture pointer
		gl.glGenTextures(2, textures, 0);

    // Front face
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
    bitmap.recycle();

    // Sides
    bitmap = loadBitmap(R.drawable.safeside);
    gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[1]);
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
    bitmap.recycle();
	}
}
