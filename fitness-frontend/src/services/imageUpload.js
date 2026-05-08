const CLOUDINARY_CLOUD_NAME = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME;
const CLOUDINARY_UPLOAD_PRESET = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET;

export const isImageUploadConfigured = () =>
  Boolean(CLOUDINARY_CLOUD_NAME && CLOUDINARY_UPLOAD_PRESET);

export const uploadActivityImage = async (file) => {
  if (!file) {
    return null;
  }

  if (!isImageUploadConfigured()) {
    throw new Error('Image upload is not configured.');
  }

  const formData = new FormData();
  formData.append('file', file);
  formData.append('upload_preset', CLOUDINARY_UPLOAD_PRESET);
  formData.append('folder', 'fitness-activities');

  const response = await fetch(
    `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`,
    {
      method: 'POST',
      body: formData
    }
  );

  if (!response.ok) {
    throw new Error('Image upload failed. Please try another image.');
  }

  const data = await response.json();
  return data.secure_url;
};
