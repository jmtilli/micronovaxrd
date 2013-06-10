% Get Debye B-factors for all the elements Z.
%
% The factors are taken from a global lookup table B_table2.
% If the table is empty, we try to load it from B_table2.txt.
% B_table2.txt must contain the B-factors in an ascii format.
function y = Btable(Z)
  global B_table2;
  if isempty(B_table2)
      fd = fopen('B_table2.txt');
      B_table2 = fscanf(fd,'%g');
      fclose(fd);
  end
  y = reshape(B_table2(Z),size(Z));
end
