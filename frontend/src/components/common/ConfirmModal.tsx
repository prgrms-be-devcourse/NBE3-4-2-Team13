interface Props {
  title: string;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
}

export default function ConfirmModal({ title, message, onConfirm, onCancel }: Props) {
  return (
    <div className='fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50'>
      <div className='bg-white dark:bg-gray-800 rounded-lg p-6 max-w-sm w-full mx-4'>
        <h2 className='text-xl font-bold mb-4'>{title}</h2>
        <p className='mb-6 text-gray-600 dark:text-gray-400'>{message}</p>
        <div className='flex justify-end gap-4'>
          <button
            onClick={onCancel}
            className='px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 transition-colors'
          >
            취소
          </button>
          <button
            onClick={onConfirm}
            className='px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors'
          >
            확인
          </button>
        </div>
      </div>
    </div>
  );
}
