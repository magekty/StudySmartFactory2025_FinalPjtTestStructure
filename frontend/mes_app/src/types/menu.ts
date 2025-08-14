export type MenuItem = {
  key: string;
  title: string;
  path: string;
  perms: { read: boolean; write: boolean; exec: boolean };
  children: MenuItem[];
};