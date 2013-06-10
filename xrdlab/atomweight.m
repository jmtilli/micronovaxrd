function y = atomweight(Z)
  global atomic_weights;
  if isempty(atomic_weights)
      fd = fopen('atomic_weights.txt');
      atomic_weights = fscanf(fd,'%g');
      fclose(fd);
  end
  y = reshape(atomic_weights(Z),size(Z));
end
