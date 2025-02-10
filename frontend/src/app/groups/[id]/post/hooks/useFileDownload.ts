import { downloadDoc } from "@/api/post/postapi";

export const usedownloadFile = () => {
  const handleDownload = async (attachmentId: number, fileName: string, token: string | null) => {
    try {
      const blob = await downloadDoc(attachmentId, token);

      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      alert("파일 다운로드 중 오류 발생: " + error);
    }
  };

  return { handleDownload };
};
