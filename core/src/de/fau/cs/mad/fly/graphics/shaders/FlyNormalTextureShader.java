package de.fau.cs.mad.fly.graphics.shaders;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;

/**
 * Created by tschaei on 21.08.14.
 */
public class FlyNormalTextureShader extends FlyBaseShader {

    private String VERTEX_SHADER = "shaders/vertex.glsl";
    private String FRAGMENT_SHADER = "shaders/normalmap.fragment.glsl";
    private int texture1, normalMap;

    @Override
    public void init() {
        createShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        super.init();

        texture1 = program.getUniformLocation("texture1");
        normalMap = program.getUniformLocation("normalMap");
    }

    @Override
    public int compareTo(Shader other) {
        return 0;
    }

    @Override
    public void render(Renderable renderable) {
        super.setUpBaseUniforms(renderable);

        //Bind textures
        ((TextureAttribute) renderable.material.get(TextureAttribute.Diffuse)).textureDescription.texture.bind(0);
        program.setUniformi(texture1, 0);

        ((TextureAttribute) renderable.material.get(TextureAttribute.Normal)).textureDescription.texture.bind(1);
        program.setUniformi(normalMap, 1);

        renderable.mesh.render(program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);
    }

}
