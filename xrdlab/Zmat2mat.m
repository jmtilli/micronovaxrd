% Convert a Zmaterial to material
function mat = Zmat2mat(mat, lambda)
  mat.B = Btable(mat.Z);
  mat.hoenl = fp(mat.Z, lambda);
  mat.lambda = lambda;
  [mat.sf_a, mat.sf_b, mat.sf_c] = sf_vectors(mat.Z);
  mat = rmfield(mat,'Z');
end
