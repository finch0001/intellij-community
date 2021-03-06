package circlet.plugins.pipelines.services.execution

import circlet.pipelines.common.api.ExecutionStatus
import circlet.pipelines.config.api.ScriptStep
import circlet.pipelines.engine.api.ServiceExecutionData
import circlet.pipelines.engine.api.storage.AContainerStepExecutionEntity
import circlet.pipelines.engine.api.storage.AGraphExecutionEntity

class CircletIdeaContainerStepExecutionEntity(
  override val id: Long,
  override var startTime: Long,
  override var endTime: Long?,
  override var status: ExecutionStatus,
  override val graph: AGraphExecutionEntity,
  override val meta: ScriptStep.Process.Container,
  override val baseVolumeName: String?,
  override val isSnapshotPoint: Boolean,
  override val volumeSize: Int?,
  override var workerId: String? = null,
  override val services: List<ServiceExecutionData> = emptyList()
) : AContainerStepExecutionEntity {
  override val metadata: MutableMap<String, Any> = hashMapOf()

  override fun equals(other: Any?): Boolean {
    return (other as? CircletIdeaContainerStepExecutionEntity)?.id == this.id
  }

  override fun hashCode(): Int {
    return this.id.hashCode()
  }
}
