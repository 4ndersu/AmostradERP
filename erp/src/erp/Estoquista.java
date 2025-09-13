package erp;

public class Estoquista extends Funcionario {
    public Estoquista(String id, String nome, double salario, String email, String senha, String lojaId) {
        super(id, nome, "Estoquista", salario, email, senha, lojaId); // Chama o construtor da classe pai
    }

    @Override
    public boolean podeCadastrarProduto() {
        return true;
    }

    @Override
    public boolean podeCadastrarDepartamento() {
        return true;
    }

    @Override
    public boolean podeCadastrarCliente() {
        return false;
    }

    @Override
    public boolean podeVenderProduto() {
        return false;
    }

    @Override
    public boolean podeVerRelatorios() {
        return false;
    }
}

