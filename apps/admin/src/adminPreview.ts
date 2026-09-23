import { ref } from 'vue';

// Only a page preview. Refresh clears it; it never represents a platform session.
const active = ref(false);

export const previewActive = active;
export function enterPreview(): void { active.value = true; }
export function leavePreview(): void { active.value = false; }
export function isPreviewActive(): boolean { return active.value; }
