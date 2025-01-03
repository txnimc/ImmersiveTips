package toni.immersivetips.foundation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import toni.immersivemessages.api.ImmersiveMessage;
import toni.immersivemessages.api.TextAnchor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ImmersiveTip {
    public static final Codec<ImmersiveTip> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.optionalFieldOf("title").forGetter((tip) -> Optional.ofNullable(tip.title)),
            Codec.STRING.optionalFieldOf("literal").forGetter((tip) -> Optional.ofNullable(tip.literal)),
            Codec.STRING.optionalFieldOf("condition").forGetter((tip) -> Optional.ofNullable(tip.condition)),
            ResourceLocation.CODEC.optionalFieldOf("translate").forGetter((tip) -> Optional.ofNullable(tip.translate)),
            Codec.FLOAT.optionalFieldOf("duration").forGetter((tip) -> Optional.of(tip.duration)),
            Codec.INT.optionalFieldOf("multiplier").forGetter((tip) -> Optional.of(tip.multiplier)),
            Priority.CODEC.optionalFieldOf("priority").forGetter((tip) -> Optional.of(tip.priority))
        ).apply(instance, ImmersiveTip::new)
    );

    private ImmersiveMessage cachedMessage;

    public String title;
    public String literal = "";
    public ResourceLocation translate;

    public Priority priority = Priority.LOW;
    public float duration = 15f;
    public int multiplier = -1;
    public String condition = "";

    public ImmersiveTip(String title, String literal) {
        this.title = title;
        this.literal = literal;
    }

    public ImmersiveTip(String title, ResourceLocation translate) {
        this.title = title;
        this.translate = translate;
    }

    public ImmersiveTip(String title, String literal, float duration) {
        this.title = title;
        this.literal = literal;
        this.duration = duration;
    }

    public ImmersiveTip(String title, ResourceLocation translate, float duration) {
        this.title = title;
        this.translate = translate;
        this.duration = duration;
    }

    public ImmersiveTip(
        Optional<String> title,
        Optional<String> literal,
        Optional<String> condition,
        Optional<ResourceLocation> translate,
        Optional<Float> duration,
        Optional<Integer> multiplier,
        Optional<Priority> priority)
    {
        this.title = title.orElse("Tip");
        this.literal = literal.orElse("");
        this.condition = condition.orElse("");
        this.translate = translate.orElse(null);
        this.duration = duration.orElse(15f);
        this.multiplier = multiplier.orElse(-1);
        this.priority = priority.orElse(Priority.MEDIUM);
    }

    public ImmersiveMessage getMessage() {
        if (cachedMessage != null)
            return cachedMessage;

        this.cachedMessage = ImmersiveMessage.builder(duration, title)
            .anchor(TextAnchor.BOTTOM_LEFT)
            .wrap()
            .y(0f)
            .size(1f)
            .slideLeft(0.3f)
            .slideOutRight(0.3f)
            .fadeIn(0.5f)
            .fadeOut(0.5f)
            .color(ChatFormatting.GOLD)
            .style(style -> style.withUnderlined(true))
            .subtext(0f, getText(), 11f, (subtext) -> subtext
                .anchor(TextAnchor.BOTTOM_LEFT)
                .wrap()
                .size(1f)
                .slideLeft(0.3f)
                .slideOutRight(0.3f)
                .fadeIn(0.5f)
                .fadeOut(0.5f)
            );

        return cachedMessage;
    }


    private String getText() {
        return translate == null ? literal : Component.translatableWithFallback(translate.getPath(), literal).getString();
    }

    public enum Priority implements StringRepresentable {
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high"),
        IMMEDIATE("immediate");

        public static final Codec<Priority> CODEC = StringRepresentable.fromEnum(Priority::values);
        public static final List<Priority> VALUES = Arrays.stream(Priority.values()).toList();

        private final String name;

        Priority(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
