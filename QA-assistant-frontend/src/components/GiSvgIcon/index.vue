<template>
  <svg
    :class="svgClass"
    :style="svgStyle"
    aria-hidden="true"
  >
    <use :xlink:href="iconName" />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  name: string
  size?: number | string
  color?: string
  class?: string
}

const props = withDefaults(defineProps<Props>(), {
  size: 16,
  color: 'currentColor'
})

const iconName = computed(() => `#icon-${props.name}`)

const svgClass = computed(() => {
  return props.class ? `gi-svg-icon ${props.class}` : 'gi-svg-icon'
})

const svgStyle = computed(() => {
  const style: Record<string, string> = {}
  
  if (props.size) {
    if (typeof props.size === 'number') {
      style.width = `${props.size}px`
      style.height = `${props.size}px`
    } else {
      style.width = props.size
      style.height = props.size
    }
  }
  
  if (props.color) {
    style.color = props.color
  }
  
  return style
})
</script>

<style scoped>
.gi-svg-icon {
  display: inline-block;
  vertical-align: middle;
  fill: currentColor;
  overflow: hidden;
}
</style>