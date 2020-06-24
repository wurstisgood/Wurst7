package net.wurstclient.mixin;

import java.util.Objects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.item.ItemRenderer;
import net.wurstclient.WurstClient;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin
{
	@Redirect(at = @At(value = "INVOKE",
		target = "Ljava/util/Objects;equals(Ljava/lang/Object;Ljava/lang/Object;)Z",
		ordinal = 0),
		method = {
			"renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformation$Mode;"
			+ "ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;"
			+ "IILnet/minecraft/client/render/model/BakedModel;)V"})
	private boolean fixArmorGlint(Object o1, Object o2)
	{
		if(WurstClient.INSTANCE.getHax().armorEspHack.enableBrightness)
			return false;
		return Objects.equals(o1, o2);
	}
}
