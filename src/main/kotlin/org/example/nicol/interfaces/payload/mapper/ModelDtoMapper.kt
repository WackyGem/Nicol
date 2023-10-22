/*
 * MIT License
 *
 * Copyright (c) 2023 Wacky Gem
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.example.nicol.interfaces.payload.mapper

import org.example.nicol.domain.inference.model.result.ModelsResult
import org.example.nicol.interfaces.payload.response.ModelsResponse
import org.mapstruct.BeanMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings
import org.mapstruct.ReportingPolicy

@Mapper(componentModel = "Spring")
interface ModelDtoMapper {

    @Mappings(
        Mapping(target = "object", constant  = "list"),
        Mapping(source = "models", target = "data")
    )
    fun toModelsResponse(modelsResult: ModelsResult): ModelsResponse

    @Mappings(
        Mapping(source = "name", target = "id"),
        Mapping(target = "object", constant = "model"),
        Mapping(source = "createdAt", target = "created"),
        Mapping(source = "username", target = "ownedBy")
    )
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    fun toModelResponse(modelResult: ModelsResult.ModelResult): ModelsResponse.ModelResponse

}