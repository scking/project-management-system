import { request } from "./request";

export const contractAttachmentApi = {
  list: (params: Record<string, unknown>) => request.get("/contract-attachments", { params }),
  create: (data: Record<string, unknown>) => request.post("/contract-attachments", data),
  upload: (contractId: number, file: File) => {
    const formData = new FormData();
    formData.append("contractId", String(contractId));
    formData.append("file", file);
    return request.post("/contract-attachments/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
  previewUrl: (id: number) => `http://127.0.0.1:9180/api/contract-attachments/${id}/download?inline=true`,
  downloadUrl: (id: number) => `http://127.0.0.1:9180/api/contract-attachments/${id}/download`,
};
