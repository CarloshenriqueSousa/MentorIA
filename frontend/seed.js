const { createClient } = require('@supabase/supabase-js');

const supabaseUrl = 'https://aegjansdipbegfgtqznu.supabase.co';
const supabaseKey = 'sb_publishable_-fQzgQxNxCFbEqKbmZVnqA_97eDj6no';

const supabase = createClient(supabaseUrl, supabaseKey);

async function seed() {
    console.log("Criando admin...");
    const { data: adminData, error: adminErr } = await supabase.auth.signUp({
        email: 'admin@mentoria.com',
        password: 'Admin123!',
        options: {
            data: { name: 'Admin MentorIA' }
        }
    });

    if (adminErr) console.error('Admin Error:', adminErr.message);
    else console.log('Admin Criado!', adminData.user?.id);

    console.log("Criando teste...");
    const { data: testData, error: testErr } = await supabase.auth.signUp({
        email: 'teste@mentoria.com',
        password: 'Teste123!',
        options: {
            data: { name: 'Usuário Teste' }
        }
    });

    if (testErr) console.error('Test Error:', testErr.message);
    else console.log('Teste Criado!', testData.user?.id);
}

seed();
