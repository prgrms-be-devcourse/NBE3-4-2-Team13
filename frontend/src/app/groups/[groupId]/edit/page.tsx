import ClientPage from './ClientPage';

interface Props {
  params: {
    id: string;
  };
}

export default function EditGroupPage({ params }: Props) {
  return <ClientPage groupId={params.id} />;
}
