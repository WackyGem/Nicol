package org.example.nicol.infrastructure.enhance

import jakarta.annotation.Resource
import org.example.nicol.infrastructure.enhance.strategy.StrategyKey
import org.example.nicol.infrastructure.enhance.strategy.StrategyMap
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
@Import(StrategyMapPostProcessorTest.Config::class)
@ComponentScan
class StrategyMapPostProcessorTest {
    @Resource
    private lateinit var modelService: ModelService

    @Configuration
    internal class Config {
        @Bean
        fun modelService() = ModelServiceImpl()

        @Bean
        fun glmGenerator() = GlmGenerator()

        @Bean
        fun llama2Generator() = Llama2Generator()
    }

    @Test
    fun test_strategies_should_success() {
        val glmOutput = modelService.chat(modelType = ModelType.GLM)
        val expectGlm = "Hello, here is glm."
        val llama2Output = modelService.chat(modelType = ModelType.LLAMA)
        val expectLlama2 = "Hi, here is llama2."
        assertThat(glmOutput, `is`(expectGlm))
        assertThat(llama2Output, `is`(expectLlama2))
    }

    enum class ModelType {
        LLAMA,
        GLM
    }

    interface ModelService {

        fun chat(modelType: ModelType) :String
    }

    interface ModelGenerator{
        @get:StrategyKey
        val type:ModelType

        fun generate(): String
    }

    class ModelServiceImpl : ModelService {

        @StrategyMap
        private lateinit var strategies: Map<ModelType, ModelGenerator>

        override fun chat(modelType: ModelType): String {
            return strategies[modelType]?.generate()?:"Not support"
        }

    }

    class Llama2Generator : ModelGenerator {
        override val type: ModelType = ModelType.LLAMA

        override fun generate(): String = "Hi, here is llama2."
    }

    class GlmGenerator : ModelGenerator {

        override val type: ModelType = ModelType.GLM

        override fun generate(): String = "Hello, here is glm."
    }


}